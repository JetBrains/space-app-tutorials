# SpaceTranslate

A .NET-based Space application that uses the [DeepL](https://www.deepl.com/) API to translate messages in Space chat.

# How to register Space UI extensions

This application registers a Space UI extension that will be shown on chat messages.

Chat messages that are not written in English can be translated to English using the [DeepL](https://www.deepl.com/) API.

## Running application locally

In `SpaceTranslate/appsettings.json`, set `DeepL:ApiKey` to an API key registered at [DeepL](https://www.deepl.com/).

For Space to be able to send events to your application, the application needs to be exposed via a public URL. One way
to do this is to use `ngrok`. After installing `ngrok` locally, run:

```shell
ngrok http https://localhost:5001/
```

See more details about setting up `ngrok`
in [Step 3 of "How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html#step-3-start-tunneling-service).

In `SpaceTranslate/appsettings.json`, set `TunnelAddress` to the public address of your ngrok tunnel.

## Installing the app into Space

This application is written as a multi-org application.
It means that it can be installed into multiple organizations,
or multiple times in a single organization.

To install the application into a particular organization,
use the full installation link that is printed to the console after starting the application.

Read more about single- and multi-org
applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

You can also register a single-org application and create webhooks manually in Space UI. See an example of a single-org
application in the source code
for ["How to Create a Chatbot" tutorial](https://www.jetbrains.com/help/space/get-started-create-a-chatbot.html).
