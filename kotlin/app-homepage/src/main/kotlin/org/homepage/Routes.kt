@file:OptIn(ExperimentalSpaceSdkApi::class)

package com.example

import com.example.processing.setAppIcon
import com.example.processing.setUiExtensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload

@OptIn(ExperimentalSpaceSdkApi::class)
fun Routing.routes() {
    post("api/myapp") {
        Space.processPayload(KtorRequestAdapter(call), ktorClient, AppInstanceStorage) { payload ->
            when (payload) {
                is InitPayload -> {
                    setAppIcon()
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

    get("api/myapp/iframe/app-homepage") {
        val backgroundColor = call.request.queryParameters["backgroundColor"] ?: "blue"

        call.respondHtml(HttpStatusCode.OK) {
            appPage(backgroundColor)
        }
    }
}

private val appCss = resourceFileAsString("app.css")
private val appJs = resourceFileAsString("app.js")

fun HTML.appPage(backgroundColor: String) {
    head {
        style {
            unsafe {
                +appCss.replace("\$backgroundColor", backgroundColor)
            }
        }

        script {
            unsafe {
                +appJs
            }
        }
    }
    body {
        div {
            id = "container"

            h1 {
                +"List of team members"
            }

            button {
                id = "authorize-button"
                style = "display:none;"
                +"Authorize to show team members"
            }

            div {
                id = "teammate-list"
            }
        }
    }
}
