package com.spacewebhooks

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import space.jetbrains.api.runtime.Batch
import space.jetbrains.api.runtime.BatchInfo
import space.jetbrains.api.runtime.helpers.readPayload
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.PingWebhookEvent
import space.jetbrains.api.runtime.types.ProfileOrganizationEvent
import space.jetbrains.api.runtime.types.TeamMembershipEvent
import space.jetbrains.api.runtime.types.WebhookRequestPayload

fun Application.configureRouting() {
    routing {
        get("/api/webhooks") {
            call.respondText("Let's handle some webhooks!", ContentType.Text.Plain)
        }

        post("/api/webhooks") {
            val body = call.receiveText()
            // verify if the request comes from a trusted Space instance
            val signature = call.request.header("X-Space-Public-Key-Signature")
            val timestamp = call.request.header("X-Space-Timestamp")?.toLongOrNull()
            // verifyWithPublicKey gets a key from Space, uses it to generate message hash
            // and compares the generated hash to the hash in a message
            if (signature.isNullOrBlank() || timestamp == null || !spaceClient.verifyWithPublicKey(
                    body, timestamp, signature
                )
            ) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val event = when (val payload = readPayload(body)) {
                // we process only payload from webhooks
                is WebhookRequestPayload -> payload.payload
                else -> error("Unexpected payload type")
            }


            when (event) {
                // process 'new user added' event
                is ProfileOrganizationEvent -> {
                    val userId = event.member.id
                    val joined = event.joinedOrganization

                    if (joined)
                        sendMessage(userId, welcomeMessage())

                    call.respond(HttpStatusCode.OK, "Sent a message to the new org member")
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

                    call.respond(HttpStatusCode.OK, "Sent a message to the new team member")
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
    var membershipBatchInfo = BatchInfo("0", 100)

    do {
        val membershipBatch = spaceClient.teamDirectory.memberships
            .getAllMemberships(batchInfo = membershipBatchInfo) {
                id()
                team {
                    name()
                }
            }

        membershipBatch.data.forEach { membership ->
            if (membershipId == membership.id) {
                return membership.team.name
            }
        }

        membershipBatchInfo = BatchInfo(membershipBatch.next, 100)
    } while (membershipBatch.hasNext())

    return null
}

// get user id by membership id
// as the getAllMemberships method may return a lot of items,
// we request memberships in batches (size 100)
private suspend fun getUserIdByMembershipId(membershipId: String): String? {
    var membershipBatchInfo = BatchInfo("0", 100)

    do {
        val membershipBatch = spaceClient.teamDirectory.memberships
            .getAllMemberships(batchInfo = membershipBatchInfo) {
                id()
                member {
                    id()
                }
            }

        membershipBatch.data.forEach { membership ->
            if (membershipId == membership.id) {
                return membership.member.id
            }
        }

        membershipBatchInfo = BatchInfo(membershipBatch.next, 100)
    } while (membershipBatch.hasNext())

    return null
}
fun Batch<*>.hasNext() = data.isNotEmpty()