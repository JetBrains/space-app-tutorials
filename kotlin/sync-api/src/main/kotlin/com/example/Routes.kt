package com.example

import com.example.db.IssueDb
import com.example.db.ProjectDb
import com.example.db.countRecords
import com.example.db.getAppInstallations
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import kotlinx.html.*
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload
import space.jetbrains.api.runtime.types.WebhookRequestPayload

@OptIn(ExperimentalSpaceSdkApi::class)
fun Application.configureRouting() {
    routing {
        // on the index page, we'll show the list of app installations
        // and the total number of projects and issues
        get("/") {
            val appInstallations = getAppInstallations()
            val projectCount = countRecords(ProjectDb)
            val issueCount = countRecords(IssueDb)

            call.respondHtml {
                head {
                    title("App Installations")
                }
                body {
                    h1 {
                        +"App Installations"
                    }
                    table {
                        thead {
                            tr {
                                th { +"Client ID" }
                                th { +"Server URL" }
                            }
                        }
                        tbody {
                            appInstallations.forEach { app ->
                                tr {
                                    td { +app.clientId }
                                    td { +app.serverUrl }
                                }
                            }
                        }
                    }
                    h1 {
                        +"Project and issue data"
                    }
                    p { +"Number of projects: $projectCount" }
                    p { +"Number of issues: $issueCount" }
                }
            }
        }

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