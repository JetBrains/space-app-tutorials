package com.example

import com.example.db.saveRefreshTokenData
import com.example.processing.createIssueSubItems
import com.example.processing.setAppIcon
import com.example.processing.setUiExtensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload
import space.jetbrains.api.runtime.types.MenuActionPayload
import space.jetbrains.api.runtime.types.RefreshTokenPayload

fun Application.configureRouting() {
    routing {
        post("api/space") {
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
                        setAppIcon()
                        setUiExtensions()
                        SpaceHttpResponse.RespondWithOk
                    }
                    is MenuActionPayload -> {
                        val result = createIssueSubItems(payload)
                        SpaceHttpResponse.RespondWithResult(result)
                    }
                    is RefreshTokenPayload -> {
                        saveRefreshTokenData(payload)
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
