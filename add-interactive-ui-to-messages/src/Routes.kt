package com.remindme

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import space.jetbrains.api.runtime.types.*
import space.jetbrains.yana.command
import space.jetbrains.yana.readPayload

fun Routing.backToSpace() {
    get("/api/back-to-space") {
        call.respondText("Hello from the remind-me bot!", ContentType.Text.Plain)
    }

    post("api/back-to-space") {
        val payload = readPayload(call.receiveText()).also {
            if (!verifyPayload(it)) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
        }

        val context = getCallContext(payload)
        val jackson = ObjectMapper()

        when (payload) {
            is MessageActionPayload -> {
                when (payload.actionId) {
                    "remind" -> {
                        // The reminder can be set on any time
                        // As this could be a really long time interval,
                        // we run commandRemind in a separate thread
                        launch { commandRemind(context, payload) }
                    }
                    else -> error("Unknown command ${payload.actionId}")
                }
                // After sending a command, Space will wait for OK confirmation
                call.respond(HttpStatusCode.OK, "")
            }
            is ListCommandsPayload -> {
                call.respondText(
                    jackson.writeValueAsString(commandListAllCommands(context)),
                    ContentType.Application.Json
                )
            }
            is MessagePayload -> {
                val command = commands.find { it.name == payload.command() }
                if (command == null) {
                    commandHelp(context)
                } else {
                    launch { command.run(context, payload) }
                }
                call.respond(HttpStatusCode.OK, "")
            }
        }
    }
}