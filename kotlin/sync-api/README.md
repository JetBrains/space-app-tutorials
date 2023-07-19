# How to sync Space entities with an external system

This sample application demonstrates how you can use the sync API to sync issues between Space and an external database. 

The application tracks the `Issue.Created`, `Issue.Deleted`, `Issue.TitleUpdated`, `Issue.DescriptionUpdated` events using a webhook. On events, the application uses the `Get sync batch` call to update issues in the PostgreSQL database.

For more information, see [Sync API](https://www.jetbrains.com/help/space/sync-api.html).

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
https://jetbrains.com/space/app/install-app?name=Sync%20API%20demo&endpoint=<endpoint-https-url-encoded>
```

`<endpoint-https-url-encoded>` should contain the complete path to the endpoint, including the `api/space` part. For
example:

```
https://jetbrains.com/space/app/install-app?name=Sync%20API%20demo&endpoint=https%3A%2F%2Fas3-94-1542-663-146.eu.ngrok.io%2Fapi%2Fspace
```

Read more about single- and multi-org
applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

