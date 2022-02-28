package com.linkpreviews

import io.ktor.http.*
import space.jetbrains.api.runtime.helpers.unfurl
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.*
import com.slack.api.methods.SlackApiException
import com.slack.api.model.Message
import java.util.concurrent.ConcurrentHashMap

data class SlackUserTokens(val accessToken: String, val refreshToken: String)

val slackUserTokens = ConcurrentHashMap<String, SlackUserTokens>()

suspend fun provideUnfurlContent(item: ApplicationUnfurlQueueItem, spaceUserId: String) {
    val url = Url(item.target)
    val parts = url.encodedPath.split('/').dropWhile { it != "archives" }.drop(1)
    val channelId = parts.firstOrNull()
    val messageId = parts.drop(1).firstOrNull()
    if (channelId == null || messageId == null)
        return

    var tokens = slackUserTokens[spaceUserId] ?: run {
        requestAuthentication(item, spaceUserId)
        return
    }

    val threadTs = url.parameters["thread_ts"]

    val message = try {
        fetchMessage(channelId, messageId, threadTs, tokens.accessToken)
    } catch (ex: SlackApiException) {
        if (ex.error.error == "token_expired") {
            val response = slackApiClient.methods().oauthV2Access {
                it
                    .clientId(SlackWorkspace.clientId)
                    .clientSecret(SlackWorkspace.clientSecret)
                    .grantType("refresh_token")
                    .refreshToken(tokens.refreshToken)
            }
            val accessToken = response.accessToken ?: response.authedUser?.accessToken
            val newRefreshToken = response.refreshToken ?: tokens.refreshToken
            if (accessToken != null) {
                tokens = SlackUserTokens(accessToken, newRefreshToken)
                slackUserTokens[spaceUserId] = SlackUserTokens(accessToken, newRefreshToken)
                fetchMessage(channelId, messageId, threadTs, tokens.accessToken)
            } else null
        } else null
    }

    if (message == null)
        return

    val channelLink = if (threadTs != null) {
        // converting message timestamp value to id for the message link (an operation opposite to `messageIdToTs`)
        val parentMessageId = "p" + threadTs.filterNot { it == '.' }
        "https://${SlackWorkspace.domain}/archives/$channelId/$parentMessageId"
    } else {
        fetchChannelName(tokens.accessToken, channelId)
    }
    val authorName = fetchAuthorName(tokens.accessToken, message.user)
    // Build link preview with message constructor DSL
    val content: ApplicationUnfurlContent.Message = unfurl {
        outline(
            MessageOutline(
                ApiIcon("slack"),
                "*$authorName* in $channelLink"
            )
        )
        section {
            text(message.text)
            text("[View message](${item.target})")
        }
    }
    spaceClient.applications.unfurls.queue.postUnfurlsContent(
        listOf(ApplicationUnfurl(item.id, content))
    )
}

suspend fun requestAuthentication(item: ApplicationUnfurlQueueItem, spaceUserId: String) {
    spaceClient.applications.unfurls.queue.requestExternalSystemAuthentication(
        item.id,
        unfurl {
            section {
                text("Authenticate in Slack to get link previews in Space")
                controls {
                    button(
                        "Authenticate",
                        NavigateUrlAction(
                            "https://<your-ngrok-hostname>/slack/oauth?user=$spaceUserId",
                            withBackUrl = true,
                            openInNewTab = false
                        )
                    )
                }
            }
        }
    )
}

fun fetchMessage(channelId: String, messageId: String, threadTs: String?, accessToken: String): Message? {
    return if (threadTs != null) {
        // https://api.slack.com/methods/conversations.replies
        slackApiClient.methods(accessToken)
            .conversationsReplies {
                it.channel(channelId).latest(threadTs).ts(messageIdToTs(messageId)).inclusive(true).limit(1)
            }
            ?.messages?.singleOrNull()
    } else {
        // https://api.slack.com/methods/conversations.history
        slackApiClient.methods(accessToken)
            .conversationsHistory {
                it.channel(channelId).latest(messageIdToTs(messageId)).inclusive(true).limit(1)
            }
            ?.messages?.singleOrNull()
    }
}

// https://api.slack.com/methods/users.info
fun fetchAuthorName(accessToken: String, slackUserId: String): String {
    return slackApiClient.methods(accessToken).usersInfo { it.user(slackUserId) }?.user?.profile?.let {
        it.displayName?.takeUnless { it.isBlank() } ?: it.realName?.takeUnless { it.isBlank() }
    } ?: slackUserId
}

// https://api.slack.com/methods/conversations.info
fun fetchChannelName(accessToken: String, channelId: String): String {
    return slackApiClient.methods(accessToken)
        .conversationsInfo { it.channel(channelId) }?.channel?.name?.let { "[#$it](https://${SlackWorkspace.domain}/archives/$channelId)" }
        ?: channelId
}

// converts slack message id as it's present in message url to the timestamp valye for use in Slack API requests
private fun messageIdToTs(messageId: String) =
    messageId.removePrefix("p").let { it.dropLast(6) + "." + it.drop(it.length - 6) }

