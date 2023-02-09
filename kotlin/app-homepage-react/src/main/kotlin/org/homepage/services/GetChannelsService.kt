package org.homepage.services

import kotlinx.serialization.Serializable
import org.homepage.appSpaceClient
import space.jetbrains.api.runtime.BatchInfo
import space.jetbrains.api.runtime.helpers.ParsedSpaceJWT
import space.jetbrains.api.runtime.resources.chats

class GetChannelsService(private val spaceJwt: ParsedSpaceJWT) {
    suspend fun getChannels(query: String): GetChannelsResponse {
        val serverUrl = spaceJwt.spaceAppInstance.spaceServer.serverUrl
        val channels = spaceJwt.appSpaceClient().chats.channels.listAllChannels(
            query,
            publicOnly = true,
            withArchived = false,
            batchInfo = BatchInfo(offset = null, batchSize = 50)
        ) {
            name()
            icon()
            channelId()
            access()
        }
            .data
            .map { ChannelOut(it.channelId, it.name, it.icon?.let { icon -> "$serverUrl/d/$icon" }) }

        return GetChannelsResponse(channels)
    }
}

@Serializable
data class GetChannelsResponse(
    val channels: List<ChannelOut>,
)

@Serializable
data class ChannelOut(
    val id: String,
    val name: String,
    val iconUrl: String?,
)
