# How to use Space webhooks

This sample application demonstrates how you can use webhooks to receive notifications
about events in Space. The application tracks two types of events:
* Adding a new user to the organization: once a user is added, the application sends a 'welcome' message to the user.
* Adding a user to a team named `MyTeam`: once a user is added to the team, the application sends an on-boarding message to the user.

This application is written as a multi-org application. It means that it can be installed into multiple organizaitons,
or multiple times in a single organization.

To install the application into a particular organization, create and follow an installation link:

```
https://<my-space-org>.jetbrains.space/extensions/installedApplications/new?name=Webhooks%20example&endpoint=<endpoint-https-url-encoded>
```

Or, to let the user pick their own organization:

```
https://jetbrains.com/space/app/install-app?name=Webhooks%20example&endpoint=<endpoint-https-url-encoded>
```

`<endpoint-https-url-encoded>` should contain the complete path to the endpoint, including the `api/space` part. For example:

```
https://jetbrains.com/space/app/install-app?name=Webhooks%20example&endpoint=https%3A%2F%2Fas3-94-1542-663-146.eu.ngrok.io%2Fapi%2Fspace
```

Read more about single- and multi-org applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

## Public URL for locally running app

For Space to be able to send your application events, the application needs to be exposed via a public URL. One way
to do this is to use `ngrok`. After installing `ngrok` locally, run:

```shell
ngrok http 8080
```

See more details about this setup in [Step 3 of "How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html#step-3-start-tunneling-service).

## Configuring local database

Application stores installation data (see `AppInstallation` table) in Postgres database. The connection parameters
for the database are set in `src/main/resources/application.conf`. If you have Docker installed, you can run a local
Postgres database with the credentials specified in `application.conf` using this command:

```shell
docker run --name space-webhooks-postgres -p 5432:5432 -e POSTGRES_USER=space-webhooks -e POSTGRES_PASSWORD=space-webhooks -e POSTGRES_DB=space-webhooks -d postgres
```

## Handling installation in the application

On installation the application server receives an `InitPayload` request from Space. On that request application
registers necessary webhooks in the Space organization that it was installed to.

You can also register a single-org application and create webhooks manually in Space UI. See an example of a single-org
application in the source code for ["How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html).
