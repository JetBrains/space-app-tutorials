package com.remindme

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.*
import space.jetbrains.api.runtime.withServiceAccountTokenSource
import space.jetbrains.yana.verifyWithToken

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