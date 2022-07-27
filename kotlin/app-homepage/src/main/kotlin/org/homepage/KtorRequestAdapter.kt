@file:OptIn(ExperimentalSpaceSdkApi::class)

package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.ktorClientForSpace

val ktorClient = ktorClientForSpace()

class KtorRequestAdapter(private val call: ApplicationCall) : RequestAdapter {
    override suspend fun receiveText() = call.receiveText()

    override fun getHeader(headerName: String) = call.request.headers[headerName]

    override suspend fun respond(httpStatusCode: Int, body: String) {
        call.respond(HttpStatusCode.fromValue(httpStatusCode), body)
    }
}
