import kotlinx.serialization.Serializable
import org.homepage.SpaceTokenInfo
import org.homepage.appSpaceClient
import space.jetbrains.api.runtime.resources.permissions
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionTarget
import space.jetbrains.api.runtime.types.PrincipalIn

class AppHasPermissionsService(private val spaceTokenInfo: SpaceTokenInfo) {
    suspend fun appHasPermissions(): AppHasPermissionsResponse {
        val hasViewChannelPermissions = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Channel.ViewChannel",
            target = GlobalPermissionTarget
        )
        val hasPostMessagesPermissions = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Channel.PostMessages",
            target = GlobalPermissionTarget
        )

        return AppHasPermissionsResponse(hasViewChannelPermissions || hasPostMessagesPermissions)
    }
}

@Serializable
data class AppHasPermissionsResponse(
    val hasPermissions: Boolean
)
