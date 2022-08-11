package org.remindme

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

fun Application.configureRouting() {
    routing {
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

            when (val payload = readPayload(body)) {
                is ListCommandsPayload -> {
                    // Space requests the list of supported commands
                    call.respondText(
                        ObjectMapper().writeValueAsString(getSupportedCommands()),
                        ContentType.Application.Json
                    )
                }
                is MessagePayload -> {
                    // user sent a message to the application
                    val commandName = payload.command()
                    val command = supportedCommands.find { it.name == commandName }
                    if (command == null) {
                        runHelpCommand(payload)
                    } else {
                        launch { command.run(payload) }
                    }
                    call.respond(HttpStatusCode.OK, "")
                }
            }
        }
    }
}
