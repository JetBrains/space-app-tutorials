package com.spacewebhooks

import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.ApiIcon
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageOutline
import space.jetbrains.api.runtime.types.MessageStyle

// message we send to all new members in our organization
fun welcomeMessage(): ChatMessage {
    return message {
        outline = MessageOutline(
            icon = ApiIcon("smile"),
            text = "Welcome to our company!"
        )
        style = MessageStyle.PRIMARY
        section {
            header = "The following links will help you to get used to the new place:"
            text("""
                * [How to set up your workplace](https://example.com)
                * [Where we usually eat lunch](https://example.com)
                * [How to take sick leave or vacation](https://example.com)
            """.trimIndent())
        }
    }
}

// message we send to members of the MyTeam team
fun myTeamMessage(): ChatMessage {
    return message {
        outline = MessageOutline(
            icon = ApiIcon("smile"),
            text = "Welcome to the MyTeam"
        )
        style = MessageStyle.PRIMARY
        section {
            header = "This info will help you to get started:"
            text("""
                * [How to get project sources](https://example.com)
                * [How to prepare environment](https://example.com)
                * [How to build the project](https://example.com)
            """.trimIndent())
        }
    }
}