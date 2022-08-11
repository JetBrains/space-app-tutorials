using SpaceTranslate.WebHook;

namespace SpaceTranslate.Startup;

public static class SpaceStartupExtensions
{
    public static WebApplicationBuilder ConfigureSpaceTranslateWebHook(this WebApplicationBuilder builder)
    {
        builder.Services.AddSpaceWebHookHandler<SpaceTranslateWebHookHandler>();
        builder.Services.AddHostedService<LogSpaceTranslateRegistrationUrlsTask>();
        
        return builder;
    }
    
    public static WebApplication MapSpaceTranslateWebHook(this WebApplication app)
    {
        app.MapSpaceWebHookHandler<SpaceTranslateWebHookHandler>("/space/receive");

        return app;
    }
}