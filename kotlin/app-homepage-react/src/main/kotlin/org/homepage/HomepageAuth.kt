package org.homepage

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.SpaceAuth
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.ParsedSpaceJWT
import space.jetbrains.api.runtime.helpers.parseAndVerifyAccessToken

suspend fun PipelineContext<Unit, ApplicationCall>.runAuthorized(handler: suspend (ParsedSpaceJWT) -> Unit) {
    getAndVerifySpaceTokenInfo()?.let { parsedSpaceJwt ->
        try {
            handler(parsedSpaceJwt)
        } catch (e: Exception) {
            log.error("Exception while processing the \"${call.request.uri}\" call from homepage UI", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    } ?: run {
        log.warn("Invalid access token presented")
        call.respond(HttpStatusCode.Unauthorized, "Invalid access token")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getAndVerifySpaceTokenInfo(): ParsedSpaceJWT? =
    (context.request.parseAuthorizationHeader() as? HttpAuthHeader.Single)?.blob
        ?.let { Space.parseAndVerifyAccessToken(it, spaceHttpClient, AppInstanceStorage) }

/**
 * Space API methods called with this client will be called on behalf of the application (using
 * Client Credentials Flow).
 */
fun ParsedSpaceJWT.appSpaceClient() =
    SpaceClient(spaceHttpClient, spaceAppInstance, SpaceAuth.ClientCredentials())

/**
 * Space API methods called with this client will be called on behalf of the user.
 */
fun ParsedSpaceJWT.userSpaceClient() =
    SpaceClient(spaceHttpClient, spaceAppInstance, SpaceAuth.Token(this.spaceAccessToken))

private val log: Logger = LoggerFactory.getLogger("Routing.kt")
