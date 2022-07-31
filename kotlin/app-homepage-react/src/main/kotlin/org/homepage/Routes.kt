import io.ktor.server.locations.*

object Routes {
    @Location("/homepage/get-channels")
    class GetChannels(val query: String)

    @Location("/homepage/send-message")
    class SendMessage(val channelId: String, val messageText: String)

    @Location("/homepage/app-has-permissions")
    object AppHasPermissions

    @Location("/api/space")
    object RequestFromSpace
}
