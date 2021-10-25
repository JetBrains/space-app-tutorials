package com.remindme

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.helpers.commandArguments
import space.jetbrains.api.runtime.types.*

suspend fun commandRemind(context: CallContext, payload: MessagePayload) {
    val args = payload.commandArguments()
    val delayMs = args?.toLongOrNull()?.times(1000)
    runTimer(context, delayMs)
}

private suspend fun runTimer(context: CallContext, delayMs: Long?) {
    if (delayMs != null) {
        sendMessage(context, acceptRemindMessage(delayMs))
        // we don't want to interrupt the thread,
        // so, we'll put our delay inside coroutineScope
        coroutineScope {
            delay(delayMs)
            sendMessage(context, remindMessage(delayMs))
        }
    } else {
        sendMessage(context, helpMessage())
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