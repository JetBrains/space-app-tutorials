package org.webhooks

import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.ApiIcon
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageOutline

fun messageToNewOrgMember(): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("happiness"),
                text = "Welcome to our company!"
            )
        )
        section {
            text("The following links will help you to get used to the new place:")
            text(
                """
                * [How to set up your workplace](https://example.com)
                * [Where we usually eat lunch](https://example.com)
                * [How to take sick leave or vacation](https://example.com)
            """.trimIndent()
            )
        }
    }
}

fun messageToNewTeamMember(): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("happiness"),
                text = "Welcome to the MyTeam"
            )
        )
        section {
            text("This info will help you to get started:")
            text(
                """
                * [How to get project sources](https://example.com)
                * [How to prepare environment](https://example.com)
                * [How to build the project](https://example.com)
            """.trimIndent()
            )
        }
    }
}
