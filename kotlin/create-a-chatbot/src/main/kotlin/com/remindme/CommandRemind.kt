package com.remindme

import kotlinx.coroutines.delay
import space.jetbrains.api.runtime.helpers.commandArguments
import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.*

suspend fun runRemindCommand(context: CallContext, payload: MessagePayload) {
    val remindMeArgs = getArgs(payload) ?: run {
        sendMessage(context, helpMessage())
        return
    }

    remindAfterDelay(context, remindMeArgs)
}

private suspend fun remindAfterDelay(context: CallContext, remindMeArgs: RemindMeArgs) {
    sendMessage(context, acceptRemindMessage(remindMeArgs))

    delay(remindMeArgs.delayMs)
    sendMessage(context, remindMessage(remindMeArgs))
}

private fun acceptRemindMessage(remindMeArgs: RemindMeArgs): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("checkbox-checked"),
                text = "Reminder accepted"
            )
        )
        section {
            text("I will remind you in ${remindMeArgs.delayMs / 1000} seconds about \"${remindMeArgs.reminderText}\"")
        }
    }
}

private fun remindMessage(remindMeArgs: RemindMeArgs): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("clock"),
                text = "Reminder"
            )
        )
        section {
            text(remindMeArgs.reminderText)
            text(
                size = MessageTextSize.SMALL,
                content = "${remindMeArgs.delayMs / 1000} seconds have passed"
            )
        }
    }
}

private fun getArgs(payload: MessagePayload): RemindMeArgs? {
    val args = payload.commandArguments() ?: return null
    val delayMs = args.substringBefore(" ").toLongOrNull()?.times(1000) ?: return null
    val reminderText = args.substringAfter(" ").trimStart().takeIf { it.isNotEmpty() } ?: return null
    return RemindMeArgs(delayMs, reminderText)
}

private class RemindMeArgs(
    val delayMs: Long,
    val reminderText: String,
)
