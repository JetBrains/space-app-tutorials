package com.example.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.resources.projects
import space.jetbrains.api.runtime.types.*

data class AppInstallation(
    val clientId: String,
    val clientSecret: String,
    val serverUrl: String
)

/**
 * Syncs issues for the given project and organization.
 * @param spaceClient Space client to use for API calls
 * @param orgId Organization ID - we use `clientId` to distinguish between app installations
 * @param prjId Project ID
 */
suspend fun syncIssues(spaceClient: SpaceClient, orgId: String, prjId: String) {
    val currentEtag = getCurrentEtag(prjId)
    // Fetch all new and updated issues and new etag using the stored etag
    val (updatedIssues, newEtag) = getIssues(spaceClient, prjId, currentEtag)

    transaction {
        // update the etag value in the ProjectDb table
        with(ProjectDb) {
            replace {
                it[id] = prjId
                it[organizationId] = orgId
                it[issueEtag] = newEtag
            }
        }

        with(IssueDb) {
            // Update the issues in the database
            for (issue in updatedIssues) {
                // When an issue is deleted in Space, it still exists in the archived state.
                // For simplicity, we'll delete archived issues from our database.
                // In a real app, you should sync issue `status` in both
                // Space and the third-party system.
                if (issue.archived) {
                    deleteWhere { id eq issue.id }
                } else {
                    replace {
                        it[this.id] = issue.id
                        it[projectId] = prjId
                        it[title] = issue.title
                        it[description] = issue.description ?: ""
                    }
                }
            }
        }
    }
}

// Fetch the latest issueEtag value for the project from the database
suspend fun getCurrentEtag(projectId: String): String {
    return transaction {
        ProjectDb.select { ProjectDb.id eq projectId }
            .singleOrNull()
            ?.get(ProjectDb.issueEtag)
            ?: "0"
    }
}

suspend fun getIssues(spaceClient: SpaceClient, projectId: String, etag: String = "0", batchSize: Int = 50): Pair<List<Issue>, String> {
    val issues = mutableListOf<Issue>()
    var latestEtag = etag
    var hasMore = true

    while (hasMore) {
        val batch = spaceClient.projects.planning.issues.getSyncBatch(
            project = ProjectIdentifier.Id(projectId),
            batchInfo = SyncBatchInfo.SinceEtag(
                etag = latestEtag,
                batchSize = batchSize
            )
        ) {
            id()
            projectId()
            title()
            description()
            archived()
        }

        issues.addAll(batch.data)
        latestEtag = batch.etag
        hasMore = batch.hasMore
    }

    return Pair(issues, latestEtag)
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


