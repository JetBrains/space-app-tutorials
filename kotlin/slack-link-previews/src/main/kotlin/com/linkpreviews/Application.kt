package com.linkpreviews

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.linkpreviews.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
