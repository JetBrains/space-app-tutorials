package com.example.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun initDbConnection() {
    try {
        Database.connect(
            "jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver",
            user = "postgres", password = "mysecretpassword"
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(AppInstallation, RefreshToken)
        }
    } catch (e: Exception) {
        logger.error("Exception while connecting to database:", e)

        val message = """Exception while connecting to Postgres database.
            
            This example app uses local Postgres database. A simple way to run local Postgres is
            to install docker and run this command:

            docker run --name my-postgres -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres

            DB tables will be created automatically by the app.

            CHANGE CREDENTIALS BEFORE USING DB IN PRODUCTION
            
        """.trimIndent()
        throw IllegalStateException(message)
    }
}

private val logger = LoggerFactory.getLogger("db")
