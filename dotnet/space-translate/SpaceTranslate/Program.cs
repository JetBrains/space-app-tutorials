using System.Runtime.CompilerServices;
using DeepL;
using SpaceTranslate.Database;
using SpaceTranslate.Startup;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSqlite<SpaceTranslateDb>("Data Source=spacetranslate.db;Cache=Shared");
builder.Services.AddDatabaseDeveloperPageExceptionFilter();
builder.Services.AddMemoryCache();
builder.Services.AddSingleton(services => new Translator(services.GetRequiredService<IConfiguration>()["DeepL:ApiKey"]));
builder.ConfigureSpaceTranslateWebHook();

var app = builder.Build();
app.EnsureDb();

app.MapSpaceTranslateWebHook();
app.MapGet("/", () => "Space app is running. See the console output for the Space installation URL.");

app.Run();