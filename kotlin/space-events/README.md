# How to use Space webhooks

This sample application demonstrates how you can use webhooks to receive notifications
about events in Space. The application tracks two types of events:

* Adding a new user to the organization: once a user is added, the application sends a 'welcome' message to the user.
* Adding a user to a team named `MyTeam`: once a user is added to the team, the application sends an on-boarding message
  to the user.

Webhooks can be created manually for your applications in Space UI, or they can be created automatically via API. This
app demonstrates the automatic creation of webhooks via API on app installation.

## Running application locally

Run `./gradlew run` command to start the application. The application uses a Postgres database which it automatically
creates in Docker during the build process. You'll need a Docker installed locally for the build to succeed.

The Postgres database is run using the default port `5432`. If you'd like to change the port, please do it in two places:

- `./docker-compose.yaml` — a configuration for DB creation during the build
- `./src/resources/application.conf` — a configuration for running the app

## Public URL for locally running app

For Space to be able to send events to your application, the application needs to be exposed via a public URL. One way
to do this is to use `ngrok`. After installing `ngrok` locally, run:

```shell
ngrok http 8080
```

See more details about setting up `ngrok`
in [Step 3 of "How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html#step-3-start-tunneling-service).

## Installing the app into Space

This application is written as a multi-org application. It means that it can be installed into multiple organizations,
or multiple times in a single organization.

To install the application into a particular organization, create and follow an installation link:

```
https://<my-space-org>.jetbrains.space/extensions/installedApplications/new?name=Webhooks%20example&endpoint=<endpoint-https-url-encoded>
```

Or, to let the user pick their own organization:

```
https://jetbrains.com/space/app/install-app?name=Webhooks%20example&endpoint=<endpoint-https-url-encoded>
```

`<endpoint-https-url-encoded>` should contain the complete path to the endpoint, including the `api/space` part. For
example:

```
https://jetbrains.com/space/app/install-app?name=Webhooks%20example&endpoint=https%3A%2F%2Fas3-94-1542-663-146.eu.ngrok.io%2Fapi%2Fspace
```

Read more about single- and multi-org
applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

On app installation into a Space organization the application server receives an `InitPayload` request from Space.
On that request application registers necessary webhooks in the Space organization that it was installed to.

You can also register a single-org application and create webhooks manually in Space UI. See an example of a single-org
application in the source code
for ["How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html).
