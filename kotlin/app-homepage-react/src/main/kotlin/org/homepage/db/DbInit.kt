package org.homepage.db

import io.ktor.http.*
import io.ktor.server.config.*
import org.homepage.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDbConnection() {
    val postgresUrl = config.tryGetString("storage.postgres.url")?.let { Url(it) }
        ?: throw IllegalArgumentException("storage.postgres.url configuration parameter not set or invalid")

    val connection = Database.connect(
        url = URLBuilder(postgresUrl).apply {
            protocol = URLProtocol("jdbc:postgresql", 5432)
            port = postgresUrl.port
            user = null
            password = null
        }.buildString(),
        driver = "org.postgresql.Driver",
        user = postgresUrl.user!!,
        password = postgresUrl.password!!
    )

    transaction(connection) {
        SchemaUtils.createMissingTablesAndColumns(AppInstallation)
    }
}
