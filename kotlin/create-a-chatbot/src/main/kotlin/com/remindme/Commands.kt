package com.remindme

import space.jetbrains.api.runtime.types.CommandDetail
import space.jetbrains.api.runtime.types.Commands
import space.jetbrains.api.runtime.types.ListCommandsPayload
import space.jetbrains.api.runtime.types.MessagePayload

/**
 * A command that the application can execute.
 */
class ApplicationCommand(
    val name: String,
    val info: String,
    val run: suspend (context: CallContext, payload: MessagePayload) -> Unit
) {
    /**
     * [CommandDetail] is returned to Space with an information about the command. List of commands
     * is shown to the user.
     */
    fun toSpaceCommand() = CommandDetail(name, info)
}

val supportedCommands = listOf(
    ApplicationCommand(
        "help",
        "Show this help",
    ) { context, _ -> runHelpCommand(context) },

    ApplicationCommand(
        "remind",
        "Remind me about something in N seconds, e.g., to remind about \"the thing\" in 10 seconds, send 'remind 10 the thing' ",
    ) { context, payload -> runRemindCommand(context, payload) }
)

/**
 * Response to the [ListCommandsPayload]. Space will display the returned commands as commands supported
 * by your application.
 */
fun getSupportedCommands() = Commands(
    supportedCommands.map {
        it.toSpaceCommand()
    }
)
