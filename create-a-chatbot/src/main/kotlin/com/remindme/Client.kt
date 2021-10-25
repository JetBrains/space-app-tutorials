package com.remindme

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.*
import space.jetbrains.api.runtime.withServiceAccountTokenSource

// url of your Space instance
private const val url = "https://jetbrains.team"
// copy-paste client-id, and client-secret
// your app got from Space
private const val clientId = "99413cf4-c2e6-4c3a-ab7d-a0d833006c77"
private const val clientSecret = "e92058be93f7d83ddf47038c3d5d747c3adbd1d6f1a7e0e35a2ec6827d11b18a"

// client for communication with Space
// it uses the Client Credentials auth flow
val spaceClient by lazy {
    SpaceHttpClient(HttpClient(CIO))
        .withServiceAccountTokenSource(
            clientId = clientId,
            clientSecret = clientSecret,
            serverUrl = url
        )
}

// verification of Space instance
// gets a key from Space, uses it to generate message hash
// and compares the generated hash to the hash in a message
suspend fun verifyRequestWithPublicKey(
    body: String, signature: String,
    timestamp: String
): Boolean {
    return spaceClient.verifyWithPublicKey(body, timestamp.toLong(), signature)
}

// get user by Id and send message to the user
// spaceClient gives you access to any Space endpoint
// CallContext is shown as an error as we haven't created it yet
suspend fun sendMessage(context: CallContext, message: ChatMessage) {
    spaceClient.chats.messages.sendMessage(
        MessageRecipient.Member(ProfileIdentifier.Id(context.userId)),
        message
    )
}
