package com.remindme

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.command
import space.jetbrains.api.runtime.helpers.readPayload
import space.jetbrains.api.runtime.helpers.verifyWithPublicKey
import space.jetbrains.api.runtime.types.ListCommandsPayload
import space.jetbrains.api.runtime.types.MessagePayload

@OptIn(ExperimentalSpaceSdkApi::class)
fun Routing.api() {

    get("/") {
        call.respondText("Hello from bot!")
    }

    post("api/space") {
        // read request body
        val body = call.receiveText()

        // verify if the request comes from a trusted Space instance
        val signature = call.request.header("X-Space-Public-Key-Signature")
        val timestamp = call.request.header("X-Space-Timestamp")?.toLongOrNull()

        // verifyWithPublicKey gets a key from Space, uses it to generate message hash
        // and compares the generated hash to the hash in a message
        if (signature.isNullOrBlank() || timestamp == null ||
            !spaceClient.verifyWithPublicKey(body, timestamp, signature)
        ) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        // read payload and get context (user id)
        val payload = readPayload(body)
        val context = getCallContext(payload)

        // JSON serializer
        val jackson = ObjectMapper()
        when (payload) {
            is ListCommandsPayload -> {
                // Space requests the list of supported commands
                call.respondText(
                    jackson.writeValueAsString(getSupportedCommands()),
                    ContentType.Application.Json
                )
            }
            is MessagePayload -> {
                // user sent a message to the application
                val command = supportedCommands.find { it.name == payload.command() }
                if (command == null) {
                    runHelpCommand(context)
                } else {
                    launch { command.run(context, payload) }
                }
                call.respond(HttpStatusCode.OK, "")
            }
        }
    }
}