# JetBrains Space Applications: examples and tutorials

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:github]
[![Slack](https://img.shields.io/badge/Slack-%23jetbrains--platform-blue?style=flat-square&logo=Slack)](https://plugins.jetbrains.com/slack)

Please see the [documentation](https://jetbrains.com/help/space/applications.html) for JetBrains Space applications.

`Kotlin` and `.NET` SDKs are currently officially supported. Corresponding examples and tutorials are in `kotlin` 
and `dotnet` directories.

## Kotlin

* [Tutorial — How to create a chatbot](https://jetbrains.com/help/space/get-started-create-a-chatbot.html). Source code for the final application is in [kotlin/create-a-chatbot](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/create-a-chatbot).
* [Tutorial — How to add interactive UI elements to messages](https://jetbrains.com/help/space/how-to-add-ui-to-messages.html). Source code for the final application is in [kotlin/add-interactive-ui-to-messages](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/add-interactive-ui-to-messages).
* [Tutorial — How to provide Slack link previews](https://www.jetbrains.com/help/space/kotlin-how-to-unfurl-links.html). Source code for the final application is in [kotlin/slack-link-previews](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/slack-link-previews).
* [Example — How to receive events from Space](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/space-events)
* [Example — Application Homepage](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/app-homepage-react). Client-server app with UI in React JS. For a client-only Application Homepage demo app see `Javascript` section below.
* [Example — Context menu extension](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/context-menu-extension)
* [Example — How to use Sync API](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/sync-api). The app syncs Space issues with a local database.

## .NET

* [Tutorial — How to create a chatbot (.NET)](https://jetbrains.com/help/space/how-to-create-a-chatbot-net.html). Source code for the final application is in [dotnet/create-a-chatbot](https://github.com/JetBrains/space-app-tutorials/tree/main/dotnet/create-a-chatbot).
* [Example — Context menu extension](https://github.com/JetBrains/space-app-tutorials/tree/main/dotnet/space-translate). Source code for an application that uses [DeepL](https://www.deepl.com) to provide translations of chat messages in Space.

## Javascript

* [Example — Application Homepage](https://github.com/JetBrains/space-app-tutorials/tree/main/js/app-homepage). A minimal example of a client-based app (only HTML/CSS/JS). For a more feature-rich version, see [client-server Application Homepage demo](https://github.com/JetBrains/space-app-tutorials/tree/main/kotlin/app-homepage-react).

## Full application source code

Source code of two production applications from Space Marketplace:

* [Slack Link Previews](https://github.com/JetBrains/space-slack-unfurls) — provides link previews between Slack and JetBrains Space in both directions
* [Slack Channel Tunnel](https://github.com/JetBrains/space-slack-channel-sync) — two-way sync of messages between Slack and Space

[jb:github]: https://github.com/JetBrains/.github/blob/main/profile/README.md
