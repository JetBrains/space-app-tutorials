package org.homepage.db

import org.homepage.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDbConnection() {
    val username = config.getString("storage.postgres.username")
    val password = config.getString("storage.postgres.password")
    val host = config.getString("storage.postgres.host")
    val port = config.getString("storage.postgres.port")
    val database = config.getString("storage.postgres.database")

    val connection = Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        driver = "org.postgresql.Driver",
        user = username,
        password = password,
    )

    transaction(connection) {
        SchemaUtils.createMissingTablesAndColumns(AppInstallation)
    }
}
