package org.homepage.db

import org.homepage.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDbConnection() {
    // see application.conf file in resources
    val host = config.getString("postgresql.host")
    val port = config.getString("postgresql.port")
    val user = config.getString("postgresql.user")
    val password = config.getString("postgresql.password")
    val database = config.getString("postgresql.database")

    Database.connect(
        "jdbc:postgresql://$host:$port/$database", driver = "org.postgresql.Driver",
        user = user, password = password
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(AppInstallation)
    }
}
