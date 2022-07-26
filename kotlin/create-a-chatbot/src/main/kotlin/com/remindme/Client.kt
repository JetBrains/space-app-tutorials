package com.remindme

import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.ktorClientForSpace
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageRecipient
import space.jetbrains.api.runtime.types.ProfileIdentifier

val spaceAppInstance = SpaceAppInstance(
    // copy-paste client-id, and client-secret
    // your app got from Space
    clientId = "client-id-assigned-to-app",
    clientSecret = "client-secret-assigned-to-app",
    // url of your Space instance
    spaceServerUrl = "https://mycompany.jetbrains.space"
)

private val spaceHttpClient = ktorClientForSpace()

// client for communication with Space
// it uses the Client Credentials auth flow
val spaceClient by lazy {
    SpaceClient(spaceHttpClient, spaceAppInstance, SpaceAuth.ClientCredentials())
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
