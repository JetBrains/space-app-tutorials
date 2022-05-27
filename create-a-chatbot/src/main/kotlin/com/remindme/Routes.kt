package com.remindme

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import space.jetbrains.api.runtime.helpers.command
import space.jetbrains.api.runtime.helpers.readPayload
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.types.ListCommandsPayload
import space.jetbrains.api.runtime.types.MessagePayload

fun Routing.api() {

    get("/") {
        call.respondText("Hello from bot!")
    }

    post("api/myapp"){
        // read request body
        val body = call.receiveText()

        // verify if the request comes from a trusted Space instance
        val signature = call.request.header("X-Space-Public-Key-Signature")
        val timestamp = call.request.header("X-Space-Timestamp")?.toLongOrNull()
        // verifyWithPublicKey gets a key from Space, uses it to generate message hash
        // and compares the generated hash to the hash in a message
        if (signature.isNullOrBlank() || timestamp == null || !spaceClient.verifyWithPublicKey(
                body, timestamp, signature
            )
        ) {
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