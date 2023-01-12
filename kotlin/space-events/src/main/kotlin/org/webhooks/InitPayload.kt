@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package org.webhooks

import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.*

@ExperimentalSpaceSdkApi
suspend fun ProcessingScope.setupWebhooks() {
    val spaceClient = clientWithClientCredentials()
    val webhook = spaceClient.applications.webhooks.createWebhook(
        application = ApplicationIdentifier.Me,
        name = "Membership events",
        description = "Track when a new member joins the organization or when existing member changes teams",
        acceptedHttpResponseCodes = listOf(200),
    )

    spaceClient.applications.webhooks.subscriptions.createSubscription(
        application = ApplicationIdentifier.Me,
        webhookId = webhook.id,
        name = "Members joining the organization",
        subscription = CustomGenericSubscriptionIn(
            subjectCode = "Members",
            filters = emptyList(),
            eventTypeCodes = listOf("Profile.Organization.Join"),
        )
    )

    spaceClient.applications.webhooks.subscriptions.createSubscription(
        application = ApplicationIdentifier.Me,
        webhookId = webhook.id,
        name = "Members joining and leaving teams",
        subscription = CustomGenericSubscriptionIn(
            subjectCode = "TeamMembership",
            filters = emptyList(),
            eventTypeCodes = listOf("Team.Membership.Created", "Team.Membership.Updated"),
        )
    )
}

@OptIn(ExperimentalSpaceSdkApi::class)
suspend fun ProcessingScope.requestPermissions() {
    val spaceClient = clientWithClientCredentials()
    spaceClient.applications.authorizations.authorizedRights.requestRights(
        application = ApplicationIdentifier.Me,
        contextIdentifier = GlobalPermissionContextIdentifier,
        listOf(PermissionIdentifier.ViewMemberships, PermissionIdentifier.ViewTeams)
    )
}
