package com.remindme

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.helpers.verifyWithToken
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ApplicationPayload
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageRecipient
import space.jetbrains.api.runtime.types.ProfileIdentifier
import space.jetbrains.api.runtime.withServiceAccountTokenSource

private val url = "https://your-space-instance.jetbrains.space"
private val clientId = "client-id-you-get-after-registering-the-app"
private val clientSecret = "client-secret-you-get-after-registering-the-app"
private val verificationToken = "endpoint-token-you-get-after-registering-the-app"

val spaceClient by lazy {
    SpaceHttpClient(HttpClient(CIO))
        .withServiceAccountTokenSource(
            clientId = clientId,
            clientSecret = clientSecret,
            serverUrl = url
        )
}

fun verifyPayload(payload: ApplicationPayload) : Boolean {
    return payload.verifyWithToken(verificationToken)
}

suspend fun sendMessage(context: CallContext, message: ChatMessage) {
    spaceClient.chats.messages.sendMessage(
        MessageRecipient.Member(ProfileIdentifier.Id(context.userId)),
        message
    )
}