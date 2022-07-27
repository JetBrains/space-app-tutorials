@file:OptIn(ExperimentalSpaceSdkApi::class)

package org.webhooks

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload
import space.jetbrains.api.runtime.types.WebhookRequestPayload

fun Application.configureRouting() {
    routing {
        post("/api/space") {

            val ktorRequestAdapter = object : RequestAdapter {
                override suspend fun receiveText() =
                    call.receiveText()

                override fun getHeader(headerName: String) =
                    call.request.header(headerName)

                override suspend fun respond(httpStatusCode: Int, body: String) =
                    call.respond(HttpStatusCode.fromValue(httpStatusCode), body)
            }

            Space.processPayload(ktorRequestAdapter, spaceHttpClient, AppInstanceStorage) { payload ->
                when (payload) {
                    is InitPayload -> {
                        setupWebhooks()
                        requestPermissions()
                    }
                    is WebhookRequestPayload -> {
                        // process webhook asynchronously, respond to Space immediately
                        launch {
                            processWebhookEvent(payload)
                        }
                    }
                }
                SpaceHttpResponse.RespondWithOk
            }
        }
    }
}
