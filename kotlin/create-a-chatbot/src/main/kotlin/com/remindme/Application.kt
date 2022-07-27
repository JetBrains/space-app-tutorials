package com.remindme

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.remindme.plugins.*

fun main() {
    embeddedServer(Netty, port = 3000) {
        configureRouting()
    }.start(wait = true)
}
