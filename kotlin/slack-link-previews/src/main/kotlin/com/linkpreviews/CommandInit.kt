package com.linkpreviews

import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.*

suspend fun commandInit(spaceUserId: String) {
    spaceClient.applications.authorizations.authorizedRights.requestRights(
        ApplicationIdentifier.Me,
        GlobalPermissionContextIdentifier,
        listOf("Unfurl.App.ProvideAttachment")
    )
    spaceClient.applications.unfurls.domains.updateUnfurledDomains(listOf(SlackWorkspace.domain))
    spaceClient.chats.messages.sendMessage(
        recipient = MessageRecipient.Member(ProfileIdentifier.Id(spaceUserId)),
        ChatMessage.Text("ok")
    )
}