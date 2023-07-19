package com.example.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class AppInstallation(
    val clientId: String,
    val clientSecret: String,
    val serverUrl: String
)

// Fetch the latest issueEtag value for the project from the database
suspend fun getCurrentEtag(projectId: String): String {
    return transaction {
        ProjectDb.select { ProjectDb.id eq projectId }
            .singleOrNull()
            ?.get(ProjectDb.issueEtag)
            ?: "0"
    }
}

suspend fun countRecords(table: Table): Long {
    return transaction {
        table.selectAll().count()
    }
}

suspend fun getAppInstallations(): List<AppInstallation> {
    return transaction {
        AppInstallationDb.selectAll().map {
            AppInstallation(
                clientId = it[AppInstallationDb.clientId],
                clientSecret = it[AppInstallationDb.clientSecret],
                serverUrl = it[AppInstallationDb.serverUrl]
            )
        }
    }
}


