using System;
using System.Net.Http;
using JetBrains.Space.Common;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace RemindMeBot
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }
        
        // This method gets called by the runtime. Use this method to add services to the container.
        // For more information on how to configure your application, visit https://go.microsoft.com/fwlink/?LinkID=398940
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddHttpClient();
            
            // Space client API
            services.AddSingleton<Connection>(provider => 
                new ClientCredentialsConnection(
                    new Uri(Configuration["Space:ServerUrl"]),
                    Configuration["Space:ClientId"],
                    Configuration["Space:ClientSecret"],
                    provider.GetService<IHttpClientFactory>().CreateClient()));
            services.AddSpaceClientApi();
            
            // Space webhook handler
            services.AddSpaceWebHookHandler<RemindMeBotHandler>(options => Configuration.Bind("Space", options));
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                // Space webhook receiver endpoint
                endpoints.MapSpaceWebHookHandler<RemindMeBotHandler>("/api/back-to-space");
                // TODO remove
                endpoints.MapSpaceWebHookHandler<RemindMeBotHandler>("/space/receive");

                endpoints.Map("/", async context =>
                {
                    context.Response.ContentType = "text/plain; charset=utf-8";
                    await context.Response.WriteAsync("Space app is running.");
                });
            });
        }
    }
}