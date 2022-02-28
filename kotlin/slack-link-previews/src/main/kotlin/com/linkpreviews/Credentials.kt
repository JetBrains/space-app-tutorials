package com.linkpreviews

import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.ktorClientForSpace
import com.slack.api.Slack

val spaceAppInstance = SpaceAppInstance(
    clientId = "<Space app client id>",
    clientSecret = "<Space app client secret>",
    spaceServerUrl = "https://<your-Space-org>.jetbrains.space"
)

private val spaceHttpClient = ktorClientForSpace()

val spaceClient by lazy {
    SpaceClient(spaceHttpClient, spaceAppInstance, SpaceAuth.ClientCredentials())
}

object SlackWorkspace {
    val clientId = "<Slack app client id>"
    val clientSecret = "<Slack app client secret>"
    val domain = "<your Slack domain>.slack.com"
}

// list of Slack permissions needed to fetch link preview contents
val slackPermissionScopes = listOf(
    "channels:history", "groups:history", "channels:read", "groups:read", "team:read", "users:read", "usergroups:read"
)

val slackApiClient = Slack.getInstance()
