package org.remindme

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main() {
    if (spaceAppInstance.clientId.isEmpty()) {
        log.error("Please specify application credentials in src/main/resources/application.conf")
        return
    }

    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}

val config: Config by lazy { ConfigFactory.load() }

val log: Logger = LoggerFactory.getLogger("ApplicationKt")
