package org.remindme

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.remindme.plugins.configureRouting

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}
