package com.remindme

import kotlinx.coroutines.delay
import space.jetbrains.api.runtime.helpers.MessageControlGroupBuilder
import space.jetbrains.api.runtime.helpers.commandArguments
import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.types.*

suspend fun runRemindCommand(context: CallContext, payload: MessagePayload) {
    val remindMeArgs = getArgs(payload)

    when {
        remindMeArgs == null -> {
            sendMessage(context, helpMessage())
        }
        remindMeArgs.delayMs == null && remindMeArgs.reminderText.isNotEmpty() -> {
            sendMessage(context, suggestRemindMessage(remindMeArgs.reminderText))
        }
        remindMeArgs.delayMs == null -> {
            sendMessage(context, helpMessage())
        }
        else -> {
            remindAfterDelay(context, remindMeArgs.delayMs, remindMeArgs.reminderText)
        }
    }
}

suspend fun runRemindCommand(context: CallContext, payload: MessageActionPayload) {
    val remindMeArgs = getArgs(payload) ?: return
    val delayMs = remindMeArgs.delayMs ?: return
    val reminderText = remindMeArgs.reminderText

    remindAfterDelay(context, delayMs, reminderText)
}

private suspend fun remindAfterDelay(context: CallContext, delayMs: Long, reminderText: String) {
    sendMessage(context, acceptRemindMessage(delayMs, reminderText))

    delay(delayMs)
    sendMessage(context, remindMessage(delayMs, reminderText))
}

private fun acceptRemindMessage(delayMs: Long, reminderText: String): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("checkbox-checked"),
                text = "Reminder accepted"
            )
        )
        section {
            text("I will remind you in ${delayMs / 1000} seconds about \"$reminderText\"")
        }
    }
}

private fun remindMessage(delayMs: Long, reminderText: String): ChatMessage {
    return message {
        outline(
            MessageOutline(
                icon = ApiIcon("clock"),
                text = "Reminder"
            )
        )
        section {
            text(reminderText)
            text(
                size = MessageTextSize.SMALL,
                content = "${delayMs / 1000} seconds have passed"
            )
        }
    }
}

private fun suggestRemindMessage(reminderText: String): ChatMessage {
    return message {
        section {
            text("Remind me in ...")
            controls {
                // buttons for 5, 60, and 300 seconds
                remindButton(5 * 1000, reminderText)
                remindButton(60 * 1000, reminderText)
                remindButton(300 * 1000, reminderText)
            }
        }
    }
}

private fun MessageControlGroupBuilder.remindButton(delayMs: Long, reminderText: String) {
    val text = "${delayMs / 1000} seconds"
    val style = MessageButtonStyle.PRIMARY
    val action = PostMessageAction("remind", "$delayMs $reminderText")
    button(text, action, style)
}


private fun getArgs(payload: MessagePayload): RemindMeArgs? {
    val args = payload.commandArguments() ?: return null
    val delayMs = args.substringBefore(" ").toLongOrNull()?.times(1000)
    val reminderText = args.substringAfter(" ").trimStart().takeIf { it.isNotEmpty() } ?: return null
    return RemindMeArgs(delayMs, reminderText)
}

private fun getArgs(payload: MessageActionPayload): RemindMeArgs? {
    val args = payload.actionValue
    val delayMs = args.substringBefore(" ").toLongOrNull() ?: return null
    val reminderText = args.substringAfter(" ").trimStart().takeIf { it.isNotEmpty() } ?: return null
    return RemindMeArgs(delayMs, reminderText)
}

private class RemindMeArgs(
    val delayMs: Long?,
    val reminderText: String,
)
