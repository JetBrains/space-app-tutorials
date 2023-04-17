package com.example

import com.example.db.AppInstallation
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.helpers.SpaceAppInstanceStorage

object AppInstanceStorage : SpaceAppInstanceStorage {
    override suspend fun loadAppInstance(clientId: String): SpaceAppInstance? {
        return transaction {
            AppInstallation.select { AppInstallation.clientId.eq(clientId) }
                .map {
                    SpaceAppInstance(
                        it[AppInstallation.clientId],
                        it[AppInstallation.clientSecret],
                        it[AppInstallation.serverUrl],
                    )
                }
                .firstOrNull()
        }
    }

    override suspend fun removeAppInstance(clientId: String): Unit = transaction {
        AppInstallation.deleteWhere { AppInstallation.clientId.eq(clientId) }
    }

    override suspend fun saveAppInstance(appInstance: SpaceAppInstance): Unit = transaction {
        with(AppInstallation) {
            replace {
                it[clientId] = appInstance.clientId
                it[clientSecret] = appInstance.clientSecret
                it[serverUrl] = appInstance.spaceServer.serverUrl
            }
        }
    }
}
