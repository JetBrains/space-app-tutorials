package com.remindme.plugins

import com.remindme.api
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        // call the api() function that handles our application endpoint
        api()
    }
}
