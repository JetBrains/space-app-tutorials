package org.webhooks

import org.webhooks.db.initDbConnection
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import space.jetbrains.api.runtime.ktorClientForSpace

fun main() {
    initDbConnection()

    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}

val spaceHttpClient = ktorClientForSpace()
