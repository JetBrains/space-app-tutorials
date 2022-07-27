package com.example

import io.ktor.server.application.*
import com.example.db.initDbConnection
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*

fun main() {
    initDbConnection()

    embeddedServer(Netty, port = 8081) {
        install(CallLogging)
        routing {
            routes()
        }
    }.start(wait = true)
}
