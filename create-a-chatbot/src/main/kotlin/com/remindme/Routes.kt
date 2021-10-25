package com.remindme

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import space.jetbrains.api.runtime.types.*
import space.jetbrains.api.runtime.helpers.command
import space.jetbrains.api.runtime.helpers.readPayload

fun Routing.api() {

    get("/") {
        call.respondText("Hello from bot!")
    }

    post("api/myapp"){
        // read request body
        val body = call.receiveText()
        // read headers required for Space verification
        val signature = call.request.header("X-Space-Public-Key-Signature")
        val timestamp = call.request.header("X-Space-Timestamp")
        // verify the request
        val verified = signature != null && timestamp != null &&
                verifyRequestWithPublicKey(body, signature, timestamp)

        if (!verified) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }
        // read payload and get context (user id)
        val payload = readPayload(body)
        val context = getCallContext(payload)
        // JSON serializer
        val jackson = ObjectMapper()

        // analyze the message payload
        // MessagePayload = user sends a command
        // ListCommandsPayload = user types a slash or a char
        when (payload) {
            is ListCommandsPayload -> {
                call.respondText(jackson.writeValueAsString(commandListAllCommands(context)),
                    ContentType.Application.Json)
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