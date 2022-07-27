package org.webhooks.db

import org.webhooks.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun initDbConnection() {
    try {
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
    } catch (e: Exception) {
        logger.error("Exception while connecting to database:", e)

        val message = """Exception while connecting to Postgres database.
            
            This example app uses local Postgres database. A simple way to run local Postgres is
            to install docker and run this command:

            docker run --name space-webhooks-postgres -p 5432:5432 -e POSTGRES_USER=space-webhooks -e POSTGRES_PASSWORD=space-webhooks -e POSTGRES_DB=space-webhooks -d postgres

            DB tables will be created automatically by the app.

            CHANGE CREDENTIALS BEFORE USING DB IN PRODUCTION
            
        """.trimIndent()
        throw IllegalStateException(message)
    }
}

private val logger = LoggerFactory.getLogger("db")
