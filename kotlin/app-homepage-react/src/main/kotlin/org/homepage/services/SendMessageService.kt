import org.homepage.SpaceTokenInfo
import org.homepage.appSpaceClient
import org.homepage.userSpaceClient
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChannelIdentifier
import space.jetbrains.api.runtime.types.ChatMessage

class SendMessageService(private val spaceTokenInfo: SpaceTokenInfo) {
    suspend fun sendMessage(channelId: String, messageText: String) {
        spaceTokenInfo.appSpaceClient().chats.messages.sendMessage(
            channel = ChannelIdentifier.Id(channelId),
            content = ChatMessage.Text(messageText)
        )
    }
}
