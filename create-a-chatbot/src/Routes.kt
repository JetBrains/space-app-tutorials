package com.remindme

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import space.jetbrains.api.runtime.helpers.command
import space.jetbrains.api.runtime.helpers.readPayload
import space.jetbrains.api.runtime.types.ListCommandsPayload
import space.jetbrains.api.runtime.types.MessageActionPayload
import space.jetbrains.api.runtime.types.MessagePayload

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