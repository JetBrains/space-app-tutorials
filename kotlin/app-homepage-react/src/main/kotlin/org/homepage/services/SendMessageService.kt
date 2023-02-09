import org.homepage.appSpaceClient
import space.jetbrains.api.runtime.helpers.ParsedSpaceJWT
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChannelIdentifier
import space.jetbrains.api.runtime.types.ChatMessage

class SendMessageService(private val spaceJwt: ParsedSpaceJWT) {
    suspend fun sendMessage(channelId: String, messageText: String) {
        spaceJwt.appSpaceClient().chats.messages.sendMessage(
            channel = ChannelIdentifier.Id(channelId),
            content = ChatMessage.Text(messageText)
        )
    }
}
