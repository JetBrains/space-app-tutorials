package com.remindme

import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.*

suspend fun commandHelp(context: CallContext) {
    sendMessage(context, helpMessage())
}

fun helpMessage(): ChatMessage {
    return message {
        outline = MessageOutline(
            icon = ApiIcon("smile"),
            text = "Remind me bot help"
        )
        section {
            header = "List of available commands"
            fields {
                commands.forEach {
                    field(it.name, it.info)
                }
            }
        }
    }
}