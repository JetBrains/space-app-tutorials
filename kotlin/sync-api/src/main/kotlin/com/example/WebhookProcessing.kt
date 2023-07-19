package com.example

import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.types.IssueWebhookEvent
import space.jetbrains.api.runtime.types.PingWebhookEvent
import space.jetbrains.api.runtime.types.WebhookRequestPayload

@OptIn(ExperimentalSpaceSdkApi::class)
suspend fun ProcessingScope.processWebhookEvent(payload: WebhookRequestPayload) {
    val spaceClient = clientWithClientCredentials()

    when (val event = payload.payload) {
        is IssueWebhookEvent -> {
            val orgId = payload.clientId
            val projectId = event.issue.projectId.toString()
            syncIssues(spaceClient, orgId, projectId)
        }

        is PingWebhookEvent -> {}

        else -> error("Unexpected event type")
    }
}
