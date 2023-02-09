import kotlinx.serialization.Serializable
import org.homepage.appSpaceClient
import space.jetbrains.api.runtime.helpers.ParsedSpaceJWT
import space.jetbrains.api.runtime.resources.permissions
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionTarget
import space.jetbrains.api.runtime.types.PermissionIdentifier
import space.jetbrains.api.runtime.types.PrincipalIn

class AppHasPermissionsService(private val spaceJwt: ParsedSpaceJWT) {
    suspend fun appHasPermissions(): AppHasPermissionsResponse {
        val hasViewChannelPermissions = spaceJwt.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            PermissionIdentifier.ViewChannelInfo,
            target = GlobalPermissionTarget
        )
        val hasPostMessagesPermissions = spaceJwt.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            PermissionIdentifier.PostMessages,
            target = GlobalPermissionTarget
        )

        return AppHasPermissionsResponse(hasViewChannelPermissions || hasPostMessagesPermissions)
    }
}

@Serializable
data class AppHasPermissionsResponse(
    val hasPermissions: Boolean
)
