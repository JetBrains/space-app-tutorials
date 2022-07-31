package org.homepage

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import org.homepage.db.initDbConnection
import space.jetbrains.api.runtime.ktorClientForSpace

@Suppress("unused")
fun Application.module() {
    initDbConnection()

    configureRouting()
}

val spaceHttpClient = ktorClientForSpace()

val config: Config by lazy { ConfigFactory.load() }
