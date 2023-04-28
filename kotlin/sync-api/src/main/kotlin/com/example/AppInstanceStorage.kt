package com.example

import com.example.db.AppInstallationDb
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import space.jetbrains.api.ExperimentalSpaceSdkApi
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.helpers.SpaceAppInstanceStorage

@ExperimentalSpaceSdkApi
object AppInstanceStorage : SpaceAppInstanceStorage {
    override suspend fun loadAppInstance(clientId: String): SpaceAppInstance? {
        return transaction {
            AppInstallationDb.select { AppInstallationDb.clientId.eq(clientId) }
                .map {
                    SpaceAppInstance(
                        it[AppInstallationDb.clientId],
                        it[AppInstallationDb.clientSecret],
                        it[AppInstallationDb.serverUrl],
                    )
                }
                .firstOrNull()
        }
    }

    override suspend fun removeAppInstance(clientId: String) : Unit = transaction  {
        AppInstallationDb.deleteWhere { AppInstallationDb.clientId.eq(clientId) }
    }

    override suspend fun saveAppInstance(appInstance: SpaceAppInstance): Unit = transaction  {
        with(AppInstallationDb) {
            replace {
                it[clientId] = appInstance.clientId
                it[clientSecret] = appInstance.clientSecret
                it[serverUrl] = appInstance.spaceServer.serverUrl
            }
        }
    }
}