using JetBrains.Space.Common;
using RemindMeBot;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddHttpClient();

// Space client API
builder.Services.AddSingleton<Connection>(provider => 
    new ClientCredentialsConnection(
        new Uri(builder.Configuration["Space:ServerUrl"]),
        builder.Configuration["Space:ClientId"],
        builder.Configuration["Space:ClientSecret"],
        provider.GetService<IHttpClientFactory>().CreateClient()));
builder.Services.AddSpaceClientApi();
            
// Space webhook handler
builder.Services.AddSpaceWebHookHandler<RemindMeBotHandler>(options => builder.Configuration.Bind("Space", options));

var app = builder.Build();
app.MapSpaceWebHookHandler<RemindMeBotHandler>("/space/receive");
app.MapGet("/", () => "Space app is running.");
app.Run();