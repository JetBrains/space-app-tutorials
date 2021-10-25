package com.remindme

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import space.jetbrains.api.runtime.SpaceHttpClient
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.*
import space.jetbrains.api.runtime.withServiceAccountTokenSource

// url of your Space instance
private const val url = "https://mycompany.jetbrains.space"
// copy-paste client-id, and client-secret
// your app got from Space
private const val clientId = "client-id-assigned-to-app"
private const val clientSecret = "client-secret-assigned-to-app"

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
