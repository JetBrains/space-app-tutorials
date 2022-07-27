@file:OptIn(ExperimentalSpaceSdkApi::class)

package com.example.processing

import com.example.resourceBytes
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.Option
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.resources.uploads
import space.jetbrains.api.runtime.types.ApplicationHomepageUiExtensionIn
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier

suspend fun ProcessingScope.setUiExtensions() {
    clientWithClientCredentials().applications.setUiExtensions(
        contextIdentifier = GlobalPermissionContextIdentifier,
        extensions = listOf(
            ApplicationHomepageUiExtensionIn(null)
        )
    )
}

suspend fun ProcessingScope.setAppIcon() {
    val client = clientWithClientCredentials()

    val imageBytes = resourceBytes("images/team.png")
    val uploadPath = client.uploads.createUpload("file")

    val serverUrl = client.server.serverUrl
    val token = client.auth.token(client.ktorClient, client.appInstance)
    val attachmentId: String = client.ktorClient.request("$serverUrl$uploadPath/issue.png") {
        method = HttpMethod.Put
        setBody(ByteArrayContent(imageBytes))
        header(HttpHeaders.Authorization, "Bearer $token")
    }.body()

    client.applications.updateApplication(ApplicationIdentifier.Me, pictureAttachmentId = Option.Value(attachmentId))
}
