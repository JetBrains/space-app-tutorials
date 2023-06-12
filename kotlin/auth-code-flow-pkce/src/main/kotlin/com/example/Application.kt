package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import space.jetbrains.api.runtime.*
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.GlobalPermissionContextIdentifier
import space.jetbrains.api.runtime.types.PermissionIdentifier
import space.jetbrains.api.runtime.types.ProfileIdentifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// store these in your database, securely and persistently.
val codeVerifiersByAuthProcessId: MutableMap<UUID, String> = ConcurrentHashMap()

// base Ktor client
val ktorClient = ktorClientForSpace()

const val spaceUrl = "https://my-company.jetbrains.space"

const val appUrl = "http://localhost:8080"
const val showUsernameRoute = "/show-username"
const val authorizeInSpaceRoute = "/authorize-in-space"
const val redirectUri = "$appUrl/$showUsernameRoute"

// class that describes your app
val spaceAppInstance = SpaceAppInstance(
    // 'clientId' and 'clientSecret' are generated when you
    // [[[register the application|https://www.jetbrains.com/help/space/register-app-in-space.html#specify-authentication-options]]] in Space
    // Do not store id and secret in plain text!
    clientId = System.getenv("JB_SPACE_CLIENT_ID"),
    clientSecret = System.getenv("JB_SPACE_CLIENT_SECRET"),
    spaceServerUrl = spaceUrl,
)

fun Application.module() {
    routing {
        // Index page
        get("/") {
            call.respondHtml {
                body {
                    button {
                        onClick = "window.location.href = '$authorizeInSpaceRoute'"
                        +"Authorize in Space and show your username"
                    }
                }
            }
        }

        // When the user presses the button, they are redirected to this page.
        // There, an ID of the auth process is generated, and a code verifier for this auth process is stored.
        // Then the user is redirected to Space for the authentication and authorization.
        get(authorizeInSpaceRoute) {
            val authProcessId = UUID.randomUUID()
            val codeVerifier = Space.generateCodeVerifier()
            codeVerifiersByAuthProcessId[authProcessId] = codeVerifier

            call.respondRedirect(
                Space.authCodeSpaceUrl(
                    appInstance = spaceAppInstance,
                    scope = PermissionScope.build(
                        PermissionScopeElement(
                            context = GlobalPermissionContextIdentifier,
                            permission = PermissionIdentifier.ViewMemberProfiles
                        )
                    ),
                    redirectUri = redirectUri,

                    /** [OAuthAccessType.OFFLINE] for offline access on behalf of user */
                    accessType = OAuthAccessType.ONLINE,
                    state = authProcessId.toString(),
                    codeVerifier = codeVerifier,
                )
            )
        }

        // After the user is authenticated in Space, they are redirected back to our app at this page.
        // With this redirect, we receive the auth code, which we can exchange for an auth token.
        // We retrieve the stored code verifier and send it to Space, so that we can be sure that the user is the same
        // person that started the auth process.
        get(showUsernameRoute) {
            suspend fun respondAuthError() {
                call.respondHtml(HttpStatusCode.Unauthorized) {
                    head {
                        title("Authentication error")
                    }
                    body {
                        p {
                            +"Authentication error."
                        }
                        button {
                            onClick = "window.location.href = '$authorizeInSpaceRoute'"
                            +"Try again"
                        }
                    }
                }
            }

            val authProcessIdString = call.parameters["state"] ?: run {
                respondAuthError()
                return@get
            }

            val authProcessId = try {
                UUID.fromString(authProcessIdString)
            } catch (_: IllegalArgumentException) {
                respondAuthError()
                return@get
            }

            val authCode = call.parameters["code"] ?: run {
                respondAuthError()
                return@get
            }

            val spaceAccessTokenInfo = try {
                Space.exchangeAuthCodeForToken(
                    ktorClient = ktorClient,
                    appInstance = spaceAppInstance,
                    authCode = authCode,
                    redirectUri = redirectUri,
                    codeVerifier = codeVerifiersByAuthProcessId[authProcessId] ?: run {
                        respondAuthError()
                        return@get
                    },
                )
            } catch (_: PermissionDeniedException) {
                respondAuthError()
                return@get
            }

            /** If you requested [OAuthAccessType.OFFLINE] access, you can use [SpaceTokenInfo.refreshToken]
             * with [SpaceAuth.RefreshToken]. */
            val spaceClient = SpaceClient(ktorClient, spaceAppInstance, SpaceAuth.Token(spaceAccessTokenInfo.accessToken))
            val profile = spaceClient.teamDirectory.profiles.getProfile(ProfileIdentifier.Me)
            val username = profile.username

            val msg = "Hello ${username}!"

            call.respondHtml {
                head {
                    title(msg)
                }
                body {
                    p {
                        +msg
                    }
                    button {
                        onClick = "window.location.href = '/'"
                        +"To home page"
                    }
                }
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
