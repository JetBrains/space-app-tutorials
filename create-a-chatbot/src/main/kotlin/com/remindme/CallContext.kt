package com.remindme

import space.jetbrains.api.runtime.types.*

// here can be any context info, e.g. user info, payload, etc.
class CallContext(
    val userId: String
)

// get userId from the payload
fun getCallContext(payload: ApplicationPayload): CallContext {
    val userId = when (payload) {
        is ListCommandsPayload -> payload.userId ?: error("no user for command")
        is MessageActionPayload -> payload.userId
        is MessagePayload -> payload.userId
        else -> error("unknown command")
    }

    return CallContext(userId)
}
