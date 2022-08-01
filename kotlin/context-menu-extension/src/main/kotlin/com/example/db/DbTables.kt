package com.example.db

import org.jetbrains.exposed.sql.Table

object AppInstallation : Table("app_installation") {
    val clientId = varchar("client_id", 36).index(isUnique = true)
    val clientSecret = varchar("client_secret", 64)
    val serverUrl = varchar("server_url", 256)

    override val primaryKey = PrimaryKey(clientId)
}

object RefreshToken : Table("refresh_token") {
    val clientId = varchar("client_id", 36)
    val userId = varchar("user_id", 20)
    val refreshToken = varchar("refresh_token", 400)
    val scope = varchar("scope", 4000)

    override val primaryKey = PrimaryKey(clientId, userId)
}
