package com.example

import com.example.db.initDbConnection
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import space.jetbrains.api.runtime.ktorClientForSpace

fun main() {
    initDbConnection()

    embeddedServer(Netty, port = 8081) {
        routing {
            routes()
        }
    }.start(wait = true)
}

val spaceHttpClient = ktorClientForSpace()
