package com.spacewebhooks

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.ktorClientForSpace

val ktorClient = ktorClientForSpace()

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalSpaceSdkApi::class)
class KtorRequestAdapter(private val call: ApplicationCall) : RequestAdapter {
    override suspend fun receiveText() = call.receiveText()

    override fun getHeader(headerName: String) = call.request.headers[headerName]

    override suspend fun respond(httpStatusCode: Int, body: String) {
        call.respond(HttpStatusCode.fromValue(httpStatusCode), body)
    }
}
