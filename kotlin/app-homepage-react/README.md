# Application Homepage

This sample application demonstrates how you can create a homepage for your
application in Space UI.

Two steps are required to add an application homepage:
* App must declare that it supports a homepage UI extension (see `InitPayload.kt`)
* App must respond with html/css that will be displayed in an iframe for the homepage

Application server uses Ktor to serve static content and process API requests. Client code is written using
React and can be found in the `./client` directory.

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
ngrok http 8081
```

See more details about setting up `ngrok`
in [Step 3 of "How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html#step-3-start-tunneling-service).

## Installing the app into Space

This application is written as a multi-org application. It means that it can be installed into multiple organizations,
or multiple times in a single organization.

To install the application into a particular organization, follow the [installation link](https://jetbrains.com/space/app/install-app?name=Homepage%20demo&endpoint=https%3A%2F%2F
space-app-homepage-example.eu-west-1.eks.intellij.net%2Fapi%2Fspace&code-flow-enabled=true&code-flow-redirect-uris=https%3A%2F%2Fnowhere.domain).

Read more about single- and multi-org
applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

On app installation into a Space organization the application server receives an `InitPayload` request from Space.
On that request application registers necessary webhooks in the Space organization that it was installed to.

You can also register a single-org application and create webhooks manually in Space UI. See an example of a single-org
application in the source code
for ["How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html).

## Messages between iframe and Space UI

You can send messages to Space web page from the iframe page using `postMessage` method in browser API. The
following message types are supported:

- `GetUserTokenRequest`
- `GetThemePropertiesRequest`
- `RedirectWithConfirmationRequest`
- `ShowConfirmDialogRequest`

Please see [the documentation](https://www.jetbrains.com/help/space/application-homepage.html#communication-between-space-and-iframe)
for details.
