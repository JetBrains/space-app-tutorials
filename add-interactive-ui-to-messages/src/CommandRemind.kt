package com.remindme

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import space.jetbrains.api.runtime.helpers.MessageControlGroupBuilder
import space.jetbrains.api.runtime.helpers.commandArguments
import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.*

suspend fun commandRemind(context: CallContext, payload: MessagePayload) {
    val args = payload.commandArguments()
    val delayMs = args?.toLongOrNull()?.times(1000)
    runTimer(context, delayMs)
}

private suspend fun runTimer(context: CallContext, delayMs: Long?) {
    if (delayMs != null) {
        sendMessage(context, acceptRemindMessage(delayMs))
        coroutineScope {
            delay(delayMs)
            sendMessage(context, remindMessage(delayMs))
        }
    } else {
        // if user doesn't specify interval show buttons
        sendMessage(context, suggestRemindMessage())
    }
}

fun acceptRemindMessage(delayMs: Long): ChatMessage {
    return message {
        outline = MessageOutline(
            icon = ApiIcon("smile"),
            text = "I will remind you in ${delayMs / 1000} seconds"
        )
    }
}

fun remindMessage(delayMs: Long): ChatMessage {
    return message {
        outline = MessageOutline(
            icon = ApiIcon("smile"),
            text = "Hey! ${delayMs / 1000} seconds are over!"
        )
    }
}

fun MessageControlGroupBuilder.remindButton(delayMs: Long) {
    val text = "${delayMs / 1000} seconds"
    val style = MessageButtonStyle.PRIMARY
    val action = PostMessageAction("remind", delayMs.toString())
    button(text, action, style)
}

fun suggestRemindMessage(): ChatMessage {
    return message {
        section {
            header = "Remind me in ..."
            controls {
                remindButton(5 * 1000)
                remindButton(60 * 1000)
                remindButton(300 * 1000)
            }
        }
    }
}

suspend fun commandRemind(context: CallContext, payload: MessageActionPayload) {
    val args = payload.actionValue
    val delayMs = args.toLongOrNull()
    runTimer(context, delayMs)
}

