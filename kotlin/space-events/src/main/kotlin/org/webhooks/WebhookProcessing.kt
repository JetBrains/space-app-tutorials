@file:OptIn(ExperimentalSpaceSdkApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package org.webhooks

import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.*

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalSpaceSdkApi::class)
suspend fun ProcessingScope.processWebhookEvent(payload: WebhookRequestPayload) {
    val client = clientWithClientCredentials()
    when (val event = payload.payload) {
        is ProfileOrganizationEvent -> {
            if (event.joinedOrganization)
                client.sendMessage(event.member.id, messageToNewOrgMember())
        }

        is TeamMembershipEvent -> {
            val membership =
                client.teamDirectory.memberships.getMembership(TeamMembershipIdentifier.Id(event.membership.id)) {
                    member {
                        id()
                        name()
                    }
                    team {
                        name()
                    }
                }

            val memberId = membership.member.id
            when (membership.team.name) {
                "MyTeam" -> client.sendMessage(memberId, messageToNewTeamMember())
            }
        }

        is PingWebhookEvent -> {}

        else -> error("Unexpected event type")
    }
}

private suspend fun SpaceClient.sendMessage(userId: String, message: ChatMessage) {
    chats.messages.sendMessage(
        channel = ChannelIdentifier.Profile(ProfileIdentifier.Id(userId)),
        content = message,
    )
}
