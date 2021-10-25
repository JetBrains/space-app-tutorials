package com.remindme.plugins

import com.remindme.api
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {
    routing {
        // call the api() function that handles our application endpoint
        api()
    }
}
