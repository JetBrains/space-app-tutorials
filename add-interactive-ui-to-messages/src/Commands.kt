package com.remindme

import space.jetbrains.api.runtime.types.*

class Command(
    val name: String,
    val info: String,
    val run: suspend (context: CallContext, payload: MessagePayload) -> Unit
) {
    // part of the protocol - returns info about a command to the chat
    fun toCommand() = CommandDetail(name, info)
}

val commands = listOf(
    Command(
        "help",
        "Show this help",
    ) { context, payload -> commandHelp(context) },

    Command(
        "remind",
        "Remind me in N seconds, e.g., to remind in 10 seconds, send 'remind 10' ",
    ) { context, payload -> commandRemind(context, payload) }
)

// this is a response to the ListCommandsPayload
// the bot must return a list of available commands
fun commandListAllCommands(context: CallContext) = Commands(
    commands.map {
        it.toCommand()
    }
)