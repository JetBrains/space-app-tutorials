package com.example.db

import org.jetbrains.exposed.sql.Table

// Represents a Space instance
// For each instance, the app stores URL and credentials
object AppInstallationDb : Table("app_installation") {
    val clientId = varchar("client_id", 36).index(isUnique = true)
    val clientSecret = varchar("client_secret", 64)
    val serverUrl = varchar("server_url", 256)
    // we use `clientId` as an organization ID
    // to distinguish between different installations of the app
    override val primaryKey = PrimaryKey(clientId)
}

// Represents a project in a Space instance
object ProjectDb : Table("project") {
    val id = varchar("id", 36)
    val organizationId = varchar("org_id", 36).references(AppInstallationDb.clientId)
    val issueEtag = varchar("issue_etag", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

// Represents an issue in a project
// For simplicity, we store and track only title and description
object IssueDb : Table("issue") {
    val id = varchar("id", 36)
    val projectId = varchar("project_id", 36).references(ProjectDb.id)
    val title = varchar("title", 255)
    val description = text("description")
    
    override val primaryKey = PrimaryKey(id)
}