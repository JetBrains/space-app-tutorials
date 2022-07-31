package org.homepage

import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.applications
import space.jetbrains.api.runtime.types.ApplicationHomepageUiExtensionIn
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier

suspend fun ProcessingScope.setUiExtensions() {
    clientWithClientCredentials().applications.setUiExtensions(
        contextIdentifier = GlobalPermissionContextIdentifier,
        extensions = listOf(
            ApplicationHomepageUiExtensionIn(iframeUrl = "/space-iframe")
        )
    )
}
