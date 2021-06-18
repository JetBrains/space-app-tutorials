package com.spacewebhooks

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import space.jetbrains.api.runtime.Batch
import space.jetbrains.api.runtime.BatchInfo
import space.jetbrains.api.runtime.helpers.*
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.*

const val signingKey = "signing-key-issued-during-app-registration"

fun Application.configureRouting() {
    routing {
        get("/api/back-to-space") {
            call.respondText("Let's handle some webhooks!", ContentType.Text.Plain)
        }

        post("/api/back-to-space") {
            val body = call.receiveText()
            println(body)

            //region Verify request from Space
            val signature = call.request.header("X-Space-Signature")
            val timestamp = call.request.header("X-Space-Timestamp")

            val verified = signature != null && timestamp != null &&
                    verifyPayloadWithSigningKey(body, signature, timestamp)

            if (!verified) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            //endregion

            val payload = readPayload(body)

            val event = when (payload) {
                // we process only payload from webhooks
                is WebhookRequestPayload -> payload.payload
                else -> error("Unexpected payload type")
            }


            when (event) {
                // process 'new user added' event
                is ProfileOrganizationEvent -> {
                    val userId = event.member.id
                    val joined = event.joinedOrganization

                    if (joined != null && joined == true)
                        sendMessage(userId, welcomeMessage())

                    call.respond(HttpStatusCode.OK, "Sent message to the new org member")
                }

                // process 'user added to a team' event
                is TeamMembershipEvent -> {
                    val membershipId = event.membership.id
                    val teamName = getTeamNameByMembershipId(membershipId)
                    val userId = getUserIdByMembershipId(membershipId)

                    if (userId != null) {
                        when (teamName) {
                            "MyTeam" -> sendMessage(userId, myTeamMessage())
                        }
                    }

                    call.respond(HttpStatusCode.OK, "Sent message to the new team member")
                }

                is PingWebhookEvent -> {
                    call.respond(HttpStatusCode.OK, "Pong")
                }
                else -> error("Unexpected event type")
            }
        }
    }
}

// get team name by membership id
// as the getAllMemberships method may return a lot of items,
// we request memberships in batches (size 100)
private suspend fun getTeamNameByMembershipId(membershipId: String): String? {
    var membershipsBatchInfo = BatchInfo("0", 100)

    do {
        val membershipsBatch = spaceClient.teamDirectory.memberships
            .getAllMemberships(batchInfo = membershipsBatchInfo) {
                id()
                team {
                    name()
                }
            }

        membershipsBatch.data.forEach { membership ->
            if (membershipId == membership.id) {
                return membership.team.name
            }
        }

        membershipsBatchInfo = BatchInfo(membershipsBatch.next, 100)
    } while (membershipsBatch.hasNext())

    return null
}

// get user id by membership id
// as the getAllMemberships method may return a lot of items,
// we request memberships in batches (size 100)
private suspend fun getUserIdByMembershipId(membershipId: String): String? {
    var membershipsBatchInfo = BatchInfo("0", 100)

    do {
        val membershipsBatch = spaceClient.teamDirectory.memberships
            .getAllMemberships(batchInfo = membershipsBatchInfo) {
                id()
                member {
                    id()
                }
            }

        membershipsBatch.data.forEach { membership ->
            if (membershipId == membership.id) {
                return membership.member?.id
            }
        }

        membershipsBatchInfo = BatchInfo(membershipsBatch.next, 100)
    } while (membershipsBatch.hasNext())

    return null
}

fun verifyPayloadWithSigningKey(body: String, signature: String, timestamp: String) : Boolean {
    val checkedSignature =
        HmacUtils(HmacAlgorithms.HMAC_SHA_256, signingKey).
        hmacHex("$timestamp:$body")
    return signature == checkedSignature
}

fun Batch<*>.hasNext() = data.isNotEmpty()