package com.spacewebhooks

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageRecipient
import space.jetbrains.api.runtime.types.ProfileIdentifier
import space.jetbrains.api.runtime.withServiceAccountTokenSource

private const val url = "https://your-space-instance.jetbrains.space"
private const val clientId = "client-id-you-get-after-registering-the-app"
private const val clientSecret = "client-secret-you-get-after-registering-the-app"

val spaceClient by lazy {
    SpaceHttpClient(HttpClient(CIO))
        .withServiceAccountTokenSource(
            clientId = clientId,
            clientSecret = clientSecret,
            serverUrl = url
        )
}

suspend fun sendMessage(userId: String, message: ChatMessage) {
    spaceClient.chats.messages.sendMessage(
        MessageRecipient.Member(ProfileIdentifier.Id(userId)),
        message
    )
}
