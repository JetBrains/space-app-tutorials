package org.remindme

import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.*

suspend fun runHelpCommand(payload: MessagePayload) {
    sendMessage(payload.userId, helpMessage())
}

fun helpMessage(): ChatMessage {
    return message {
        MessageOutline(
            icon = ApiIcon("checkbox-checked"),
            text = "Remind me bot help"
        )
        section {
            text("List of available commands", MessageStyle.PRIMARY)
            fields {
                supportedCommands.forEach {
                    field(it.name, it.info)
                }
            }
        }
    }
}
