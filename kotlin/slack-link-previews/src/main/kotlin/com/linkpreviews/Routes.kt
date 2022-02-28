package com.linkpreviews

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import space.jetbrains.api.runtime.helpers.readPayload
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.types.MessagePayload
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.NewUnfurlQueueItemsPayload
import space.jetbrains.api.runtime.types.ProfileIdentifier
import java.util.concurrent.ConcurrentHashMap

private var lastEtag: Long? = null

fun Routing.api() {
    post("api/space") {
        // read request body
        val body = call.receiveText()

        // verify the request
        val signature = call.request.header("X-Space-Public-Key-Signature")
        val timestamp = call.request.header("X-Space-Timestamp")?.toLongOrNull()
        if (signature.isNullOrBlank() || timestamp == null || !spaceClient.verifyWithPublicKey(
                body,
                timestamp,
                signature
            )
        ) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        // read and process the message payload
        when (val payload = readPayload(body)) {
            is MessagePayload -> {
                if ((payload.message.body as? ChatMessage.Text)?.text == "init") {
                    commandInit(payload.userId)
                }
                call.respond(HttpStatusCode.OK, "")
            }
            is NewUnfurlQueueItemsPayload -> {
                val queueApi = spaceClient.applications.unfurls.queue
                var queueItems = queueApi.getUnfurlQueueItems(lastEtag, batchSize = 100)
                while (queueItems.isNotEmpty()) {
                    queueItems.forEach { item ->
                        if (item.authorUserId != null && item.target.startsWith("https://${SlackWorkspace.domain}/archives")) {
                            val spaceUserId = (item.authorUserId as? ProfileIdentifier.Id)?.id
                                ?: error("ProfileIdentifier.Id")
                            provideUnfurlContent(item, spaceUserId)
                        }
                    }
                    lastEtag = queueItems.last().etag
                    queueItems = queueApi.getUnfurlQueueItems(lastEtag, batchSize = 100)
                }
            }
        }
    }

    get("slack/oauth") {
        val spaceUserId = call.parameters.get("user") ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                "user parameter expected"
            )
            return@get
        }
        val backUrl = call.parameters.get("backUrl") ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                "backUrl parameter expected"
            )
            return@get
        }
        val flowId = generateNonce()
        oAuthSessions[flowId] = OAuthSession(spaceUserId, backUrl)
        val authUrl = with(URLBuilder("https://${SlackWorkspace.domain}/oauth/v2/authorize")) {
            parameters.apply {
                append("client_id", SlackWorkspace.clientId)
                append("user_scope", slackPermissionScopes.joinToString(","))
                append("state", flowId)
                // replace it with your ngrok tunnel public address
                append("redirect_uri", "https://<your-ngrok-hostname>/slack/oauth/callback")
            }
            build()
        }
        call.respondRedirect(authUrl.toString())
    }

    get("slack/oauth/callback") {
        val flowId = call.parameters.get("state") ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                "state parameter expected"
            )
            return@get
        }
        val session = oAuthSessions.get(flowId) ?: run {
            call.respond(
                HttpStatusCode.Unauthorized,
                "invalid auth session"
            )
            return@get
        }
        val code = call.parameters.get("code") ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                "code parameter expected"
            )
            return@get
        }
        val slackUserToken = requestOAuthToken(code)
        if (slackUserToken == null) {
            call.respond(HttpStatusCode.Unauthorized, "could not fetch OAuth token from Slack")
            return@get
        }
        slackUserTokens[session.spaceUserId] = slackUserToken
        spaceClient.applications.unfurls.queue.clearExternalSystemAuthenticationRequests(
            ProfileIdentifier.Id(session.spaceUserId)
        )
        call.respondRedirect(session.backUrl)
    }
}

// key - generated nonce, value - Space user id
val oAuthSessions = ConcurrentHashMap<String, OAuthSession>()

data class OAuthSession(val spaceUserId: String, val backUrl: String)

fun requestOAuthToken(code: String): SlackUserTokens? {
    val response = slackApiClient.methods()
        .oauthV2Access { it.clientId(SlackWorkspace.clientId).clientSecret(SlackWorkspace.clientSecret).code(code) }
    return response?.takeIf { it.isOk }?.authedUser?.takeIf { it.accessToken != null && it.refreshToken != null }
        ?.let { SlackUserTokens(it.accessToken, it.refreshToken) }
}