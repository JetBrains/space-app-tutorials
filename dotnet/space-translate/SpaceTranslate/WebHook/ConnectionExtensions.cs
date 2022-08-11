using JetBrains.Space.Common;
using SpaceTranslate.Database;

namespace SpaceTranslate.WebHook;

public static class ConnectionExtensions
{
    public static ClientCredentialsConnection CreateConnection(this Organization organization) =>
        new(
            serverUrl: new Uri(organization.ServerUrl), 
            clientId: organization.ClientId,
            clientSecret: organization.ClientSecret);
    
    public static RefreshTokenConnection? CreateConnection(this User user)
    {
        if (string.IsNullOrEmpty(user.RefreshToken)) return null;

        return new RefreshTokenConnection(
            serverUrl: new Uri(user.Organization.ServerUrl),
            clientId: user.Organization.ClientId,
            clientSecret: user.Organization.ClientSecret,
            scopes: new[] { user.Scope ?? "global:Channel.ViewMessages" },
            authenticationTokens: AuthenticationTokens.FromRefreshToken(user.RefreshToken));
    }
}