package org.webhooks

import io.ktor.server.application.*
import org.webhooks.db.initDbConnection
import space.jetbrains.api.runtime.ktorClientForSpace

@Suppress("unused")
fun Application.module() {
    initDbConnection()

    configureRouting()
}

val spaceHttpClient = ktorClientForSpace()
