package com.example

import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.*

@ExperimentalSpaceSdkApi
suspend fun ProcessingScope.setupWebhooks() {
    val spaceClient = clientWithClientCredentials()

    // create a webhook for the app
    val webhook = spaceClient.applications.webhooks.createWebhook(
        application = ApplicationIdentifier.Me,
        name = "Issue and project events",
        description = "Track when an issue is created, or a title or a description is updated",
        acceptedHttpResponseCodes = listOf(200),
        payloadFields = {
            issue()
        }
    )

    // for the created webhook, create a subscription for the events we want to receive
    spaceClient.applications.webhooks.subscriptions.createSubscription(
        application = ApplicationIdentifier.Me,
        webhookId = webhook.id,
        name = "Issues created, deleted, or updated",
        subscription = CustomGenericSubscriptionIn(
            subjectCode = "Issue",
            filters = emptyList(),
            eventTypeCodes = listOf("Issue.Created", "Issue.Deleted", "Issue.TitleUpdated", "Issue.DescriptionUpdated")
        )
    )
}

// The app needs the permission to view issue data
@OptIn(ExperimentalSpaceSdkApi::class)
suspend fun ProcessingScope.requestPermissions() {
    val spaceClient = clientWithClientCredentials()
    spaceClient.applications.authorizations.authorizedRights.requestRights(
        application = ApplicationIdentifier.Me,
        contextIdentifier = GlobalPermissionContextIdentifier,
        listOf(PermissionIdentifier.ViewIssues)
    )
}

