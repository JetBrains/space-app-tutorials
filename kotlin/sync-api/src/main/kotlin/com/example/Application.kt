package com.example

import com.example.db.initDbConnection
import io.ktor.server.application.*
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import space.jetbrains.api.runtime.ktorClientForSpace

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    initDbConnection()
    configureRouting()
}

val spaceHttpClient = ktorClientForSpace()

val config: Config by lazy { ConfigFactory.load() }