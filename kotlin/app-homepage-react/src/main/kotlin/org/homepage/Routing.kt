package org.homepage

import AppHasPermissionsService
import Routes
import SendMessageService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.homepage.services.GetChannelsService
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload

fun Application.configureRouting() {
    install(Locations)
    install(ContentNegotiation) {
        json()
    }

    routing {
        static("/space-iframe") {
            staticBasePackage = "space-iframe"
            resources(".")
            defaultResource("index.html")
        }
        static("/") {
            staticBasePackage = "space-iframe"
            resources(".")
        }

        get<Routes.GetChannels> { params ->
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, GetChannelsService(spaceTokenInfo).getChannels(params.query))
            }
        }

        post<Routes.SendMessage> { params ->
            runAuthorized { spaceTokenInfo ->
                SendMessageService(spaceTokenInfo).sendMessage(params.channelId, params.messageText)
                call.respond(HttpStatusCode.OK)
            }
        }

        get<Routes.AppHasPermissions> {
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, AppHasPermissionsService(spaceTokenInfo).appHasPermissions())
            }
        }

        post<Routes.RequestFromSpace> {
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
                        setUiExtensions()
                        SpaceHttpResponse.RespondWithOk
                    }
                    else -> {
                        call.respond(HttpStatusCode.OK)
                        SpaceHttpResponse.RespondWithOk
                    }
                }
            }
        }
    }
}
