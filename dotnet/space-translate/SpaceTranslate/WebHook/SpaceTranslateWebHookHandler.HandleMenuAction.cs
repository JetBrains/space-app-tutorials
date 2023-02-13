using JetBrains.Space.Client;
using JetBrains.Space.Common;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;
using SpaceTranslate.Database;

namespace SpaceTranslate.WebHook;

// TODO comments everywhere

public partial class SpaceTranslateWebHookHandler
{
    public override async Task<AppUserActionExecutionResult> HandleMenuActionAsync(MenuActionPayload payload)
    {
        using var loggerScopeForClientId = _logger.BeginScope("ClientId={ClientId}", payload.ClientId);
        
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist");
            return AppUserActionExecutionResult.Failure("The organization does not exist.");
        }
        
        using var loggerScopeForUserId = _logger.BeginScope("UserId={UserId}", payload.UserId);
        
        if (payload.Context is not ChannelMessageMenuActionContext actionContext)
        {
            _logger.LogWarning("Unknown payload context type. ContextType={ContextType}", payload.Context?.GetType());
            return AppUserActionExecutionResult.Failure("The payload could not be processed.");
        }

        if (actionContext.ChannelIdentifier is not ChannelIdentifier.ChannelIdentifierId channelIdentifierId)
        {
            _logger.LogWarning("Unknown channel identifier type. ChannelIdentifierType={ChannelIdentifierType}", actionContext.ChannelIdentifier.GetType().Name);
            return AppUserActionExecutionResult.Failure("The payload could not be processed.");
        }

        var user = await _db.Users
            .Include(m => m.Organization)
            .FirstOrDefaultAsync(it => it.OrganizationId == organization.Id && it.UserId == payload.UserId);
        if (user == null)
        {
            _logger.LogWarning("User-specific permissions required (no cached credential)");
            return PermissionsRequired(null, channelIdentifierId);
        }

        var organizationConnection = organization.CreateConnection();
        var userConnection = user.CreateConnection();
        if (userConnection == null)
        {
            _logger.LogWarning("User-specific permissions required (no cached credential)");
            return PermissionsRequired(user.Scope, null);
        }
        
        var organizationChatClient = new ChatClient(organizationConnection);
        var userChatClient = new ChatClient(userConnection);

        ChannelItemRecord? originalMessage = null;
        M2ChannelRecord? originalMessageChannelInfo = null;
        try
        {
            originalMessage = await userChatClient.Messages.GetMessageAsync(
                actionContext.MessageIdentifier,
                actionContext.ChannelIdentifier,
                _ => _
                    .WithAllFieldsWildcard());
            
            // Try accessing text, if it is not accessible we need additional permissions
            var _ = originalMessage.Text;
            
            originalMessageChannelInfo = await userChatClient.Channels.GetChannelAsync(actionContext.ChannelIdentifier);
        }
        catch (PermissionDeniedException)
        {
            _logger.LogWarning("User-specific permissions required (permission denied)");
            return PermissionsRequired(user.Scope, channelIdentifierId);
        }
        catch (RefreshTokenRevokedException)
        {
            _logger.LogWarning("User-specific permissions required (refresh token revoked)");
            return PermissionsRequired(user.Scope, channelIdentifierId);
        }
        catch (PropertyNotRequestedException)
        {
            _logger.LogWarning("User-specific permissions required (property not accessible)");
            return PermissionsRequired(user.Scope, channelIdentifierId);
        }
        
        if (userConnection.AuthenticationTokens?.RefreshToken != null &&
            userConnection.AuthenticationTokens.RefreshToken != user.RefreshToken)
        {
            user.RefreshToken = userConnection.AuthenticationTokens.RefreshToken;
            await _db.SaveChangesAsync();
        }

        var cacheKey = organization.Id + "__" +
                       actionContext.ChannelIdentifier + "__" +
                       actionContext.MessageIdentifier + "__" +
                       originalMessage.Text.ToMd5();

        var cachedTranslation = await _cache.GetOrCreateAsync(cacheKey, async entry =>
        {
            if (string.IsNullOrWhiteSpace(originalMessage.Text)) return "(empty)";
            
            _logger.LogInformation("Requesting translation from DeepL...");
                
            var translatedText = await _translator.TranslateTextAsync(
                text: originalMessage.Text,
                sourceLanguageCode: null,
                targetLanguageCode: "en-US",
                options: null);
            
            _logger.LogInformation("Received translation from DeepL. DetectedSourceLanguageCode={DetectedSourceLanguageCode}", translatedText.DetectedSourceLanguageCode);

            return translatedText.Text;
        });

        if (cachedTranslation != null)
        {
            var channelInfoName = "original message";
            if (originalMessageChannelInfo.Contact.Ext is M2SharedChannelContent channelContent)
            {
                channelInfoName = $"original message in #{channelContent.Name}";
            }
            
            await organizationChatClient.Messages.SendMessageAsync(
                recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
                content: ChatMessage.Block(new List<MessageSectionElement>
                {
                    MessageSectionElement.MessageSection(new List<MessageBlockElement>
                    {
                        MessageBlockElement.MessageText(
                            $"Translation of [{channelInfoName}]({organization.ServerUrl}/im/translated/?message={actionContext.MessageIdentifier.ToString()!.Replace("id:", "").Replace("externalId:", "")}&channel={actionContext.ChannelIdentifier.ToString()!.Replace("id:", "")}): "),
                        MessageBlockElement.MessageDivider(),
                        MessageBlockElement.MessageText(cachedTranslation)
                    })
                }));
        }
        else
        {
            _logger.LogWarning("Could not translate message");
            return AppUserActionExecutionResult.Failure("Could not translate message.");
        }

        return await base.HandleMenuActionAsync(payload);
    }
    
    private AppUserActionExecutionResult PermissionsRequired(
        string? existingScope,
        ChannelIdentifier.ChannelIdentifierId? channelIdentifier)
    {
        // Always request the global scopes
        var permissionScopeElements = new HashSet<PermissionScopeElement>
        {
            new(PermissionContextIdentifier.Global, PermissionIdentifier.ViewMessages),
            new(PermissionContextIdentifier.Global, PermissionIdentifier.ViewChannelInfo),
            new(PermissionContextIdentifier.Global, PermissionIdentifier.ViewDirectMessages)
        };

        // For private channels, channel-specific scopes are needed
        if (channelIdentifier != null)
        {
            permissionScopeElements.Add(new(PermissionContextIdentifier.Channel(channelIdentifier.Id), PermissionIdentifier.ViewMessages));
            permissionScopeElements.Add(new(PermissionContextIdentifier.Channel(channelIdentifier.Id), PermissionIdentifier.ViewChannelInfo));
        }

        return AppUserActionExecutionResult.AuthCodeFlowRequired(
            new List<AuthCodeFlowPermissionsRequest>
            {
                new (new PermissionScope(existingScope ?? "") + PermissionScopeBuilder.FromElements(permissionScopeElements), purpose: "translate chat message")
            });
    }

    public override async Task<ApplicationExecutionResult> HandleRefreshTokenAsync(RefreshTokenPayload payload)
    {
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist. ClientId={ClientId}", payload.ClientId);
            return new ApplicationExecutionResult("The organization does not exist.", 400);
        }
        
        var user = await _db.Users
            .Include(m => m.Organization)
            .FirstOrDefaultAsync(it => it.OrganizationId == organization.Id && it.UserId == payload.UserId);
        if (user == null)
        {
            user = new User
            {
                OrganizationId = organization.Id,
                UserId = payload.UserId,
                Created = DateTimeOffset.UtcNow
            };
            _db.Users.Add(user);
        }

        user.Scope = payload.Scope;
        user.RefreshToken = payload.RefreshToken;

        await _db.SaveChangesAsync();
            
        return await base.HandleRefreshTokenAsync(payload);
    }
}