package com.spacewebhooks

import com.spacewebhooks.db.initDbConnection
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    initDbConnection()

    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}