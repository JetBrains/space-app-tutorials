using JetBrains.Space.Client;
using Microsoft.EntityFrameworkCore;
using SpaceTranslate.Database;

namespace SpaceTranslate.WebHook;

public partial class SpaceTranslateWebHookHandler
{
    public override async Task<ApplicationExecutionResult> HandleInitAsync(InitPayload payload)
    {
        // Validation
        if (payload.State == null)
        {
            _logger.LogWarning("No state parameter is provided in the in the request payload");
            return new ApplicationExecutionResult("No state parameter is provided in the request payload.", 400);
        }

        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization != null)
        {
            _logger.LogWarning("The organization is already registered. ClientId={ClientId}; ServerUrl={ServerUrl}", payload.ClientId, payload.ServerUrl);
            return new ApplicationExecutionResult("The organization server URL is already registered.", 400);
        }

        // Create organization locally
        organization = new Organization
        {
            Created = DateTimeOffset.UtcNow,
            ServerUrl = payload.ServerUrl,
            ClientId = payload.ClientId,
            ClientSecret = payload.ClientSecret,
            UserId = payload.UserId,
            SigningKey = "pending"
        };

        _db.Organizations.Add(organization);

        await _db.SaveChangesAsync();
        
        // Connect to Space
        var connection = organization.CreateConnection();
        var applicationClient = new ApplicationClient(connection);

        // Store signing key
        var signingKey = await applicationClient.SigningKey.GetSigningKeyAsync(ApplicationIdentifier.Me);
        organization.SigningKey = signingKey;
        await _db.SaveChangesAsync();
        
        // Initialize Space organization
        var applicationInfo = await applicationClient.GetApplicationAsync(ApplicationIdentifier.Me);

        if (string.IsNullOrEmpty(applicationInfo.Picture))
        {
            await using var logoStream = GetType().Assembly.GetManifestResourceStream("SpaceTranslate.Resources.logo.png")!;
            
            var uploadClient = new UploadClient(connection);
            var uploadedFileAttachmentId = await uploadClient.UploadAsync(
                storagePrefix: "file",
                fileName: "spacetranslate-logo.png",
                uploadStream: logoStream,
                mediaType: null);
        
            if (!string.IsNullOrEmpty(uploadedFileAttachmentId))
            {
                await applicationClient.UpdateApplicationAsync(
                    application: ApplicationIdentifier.Me,
                    pictureAttachmentId: uploadedFileAttachmentId);
            }
        }
            
        await applicationClient.Authorizations.AuthorizedRights.RequestRightsAsync(
            application: ApplicationIdentifier.Me,
            contextIdentifier: PermissionContextIdentifier.Global, 
            rightCodes: new List<PermissionIdentifier>
            {
                PermissionIdentifier.ViewMemberProfiles,
                PermissionIdentifier.ViewMessages,
                PermissionIdentifier.ViewChannelInfo
            });
        
        await applicationClient.SetUiExtensionsAsync(
            contextIdentifier: PermissionContextIdentifier.Global,
            extensions: new List<AppUiExtensionIn>
            {
                new ChatMessageMenuItemUiExtensionIn(
                    displayName: "Translate",
                    description: "Translates the message into English.",
                    menuItemUniqueCode: "translate-message",
                    visibilityFilters: new List<ChatMessageMenuItemVisibilityFilterIn>()) // no filters - visible to everyone
            });

        return await base.HandleInitAsync(payload);
    }

    public override async Task<ApplicationExecutionResult> HandleChangeClientSecretRequestAsync(ChangeClientSecretPayload payload)
    {
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist. ClientId={ClientId}", payload.ClientId);
            return new ApplicationExecutionResult("The organization does not exist.", 400);
        }

        organization.ClientSecret = payload.NewClientSecret;
        await _db.SaveChangesAsync();
        
        return await base.HandleChangeClientSecretRequestAsync(payload);
    }

    public override async Task<ApplicationExecutionResult> HandleChangeServerUrlAsync(ChangeServerUrlPayload payload)
    {
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist. ClientId={ClientId}", payload.ClientId);
            return new ApplicationExecutionResult("The organization does not exist.", 400);
        }

        organization.ServerUrl = payload.NewServerUrl;
        await _db.SaveChangesAsync();
            
        return await base.HandleChangeServerUrlAsync(payload);
    }

    public override async Task<ApplicationExecutionResult> HandleUninstalledAsync(ApplicationUninstalledPayload payload)
    {
        // Uninstall application
        var organization = await _db.Organizations
            .Include(it => it.Users)
            .FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization is already uninstalled. ClientId={ClientId}; ServerUrl={ServerUrl}", payload.ClientId, payload.ServerUrl);
            return new ApplicationExecutionResult("The organization is already uninstalled.");
        }

        // Delete related entities
        _db.RemoveRange(organization.Users);
        _db.RemoveRange(organization);
        await _db.SaveChangesAsync();

        return new ApplicationExecutionResult("The organization has been uninstalled.");
    }
}