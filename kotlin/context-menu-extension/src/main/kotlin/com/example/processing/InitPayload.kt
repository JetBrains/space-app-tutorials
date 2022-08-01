package com.example.processing

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import space.jetbrains.api.runtime.Option
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.resources.uploads
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier
import space.jetbrains.api.runtime.types.IssueEditableByMe
import space.jetbrains.api.runtime.types.IssueMenuItemUiExtensionIn

suspend fun ProcessingScope.setUiExtensions() {
    clientWithClientCredentials().applications.setUiExtensions(
        // add issue menu item globally, for all projects
        contextIdentifier = GlobalPermissionContextIdentifier,
        // the list of extensions: only one menu item
        extensions = listOf(
            IssueMenuItemUiExtensionIn(
                // what menu item will say
                displayName = "Create sub-tasks",
                // menu item description for app home page
                description = "Create sub-tasks for current issue",
                // identify the menu item when it's clicked
                menuItemUniqueCode = "create-sub-tasks",
                // only display for people who can edit the issue
                visibilityFilters = listOf(IssueEditableByMe),
            )
        )
    )
}

suspend fun ProcessingScope.setAppIcon() {
    val client = clientWithClientCredentials()

    val imageBytes = resourceBytes("images/issue.png")
    val uploadPath = client.uploads.createUpload("file")

    val serverUrl = client.server.serverUrl
    val token = client.auth.token(client.ktorClient, client.appInstance)
    val attachmentId = client.ktorClient.put("$serverUrl$uploadPath/issue.png") {
        setBody(ByteArrayContent(imageBytes))
        header(HttpHeaders.Authorization, "Bearer $token")
    }.bodyAsText()

    client.applications.updateApplication(ApplicationIdentifier.Me, pictureAttachmentId = Option.Value(attachmentId))
}

private fun ProcessingScope.resourceBytes(resourcePath: String): ByteArray {
    val inputStream =
        this::class.java.classLoader.getResourceAsStream(resourcePath) ?: error("Could not read resource $resourcePath")
    return inputStream.use { it.readBytes() }
}
