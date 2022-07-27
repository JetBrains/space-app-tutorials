package org.remindme

import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.ApiIcon
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageOutline
import space.jetbrains.api.runtime.types.MessageStyle

suspend fun runHelpCommand(context: CallContext) {
    sendMessage(context, helpMessage())
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
