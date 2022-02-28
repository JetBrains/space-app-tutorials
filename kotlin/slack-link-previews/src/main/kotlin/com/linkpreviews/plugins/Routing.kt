package com.linkpreviews.plugins

import com.linkpreviews.api
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        api()
    }
}
