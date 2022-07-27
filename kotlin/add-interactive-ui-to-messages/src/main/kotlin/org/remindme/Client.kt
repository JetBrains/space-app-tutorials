package org.remindme

import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.ktorClientForSpace
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChannelIdentifier
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageRecipient
import space.jetbrains.api.runtime.types.ProfileIdentifier

val spaceAppInstance = SpaceAppInstance(
    /**
     * Copy client id and client secret from the `Authentication` tab
     * on the application page for your app in Space.
     *
     * Client Secret is a sensitive value. Normally you would pass it to the application
     * as an environment variable.
     */
    clientId = "884541a0-b31f-4305-9e6f-ecab5dc9d675",
    clientSecret = "fe2e6b3741629dfb4e7389cc1aede9699d9b7c9ce60b140b1ef2986cc8717ed0",
    /**
     * URL of your Space instance
     */
    spaceServerUrl = "https://elbrus-test.jetbrains.space/"
)

val spaceHttpClient = ktorClientForSpace()

/**
 * Space Client used to call API methods in Space.
 * Note the usage of [SpaceAuth.ClientCredentials] for authorization: the application will
 * authorize in Space based on clientId+clientSecret and will act on behalf of itself (not
 * on behalf of a Space user).
 */
val spaceClient =
    SpaceClient(ktorClient = spaceHttpClient, appInstance = spaceAppInstance, auth = SpaceAuth.ClientCredentials())

/**
 * Call API method in Space to send a message to the user.
 */
suspend fun sendMessage(context: CallContext, message: ChatMessage) {
    spaceClient.chats.messages.sendMessage(
        channel = ChannelIdentifier.Profile(ProfileIdentifier.Id(context.userId)),
        content = message
    )
}
