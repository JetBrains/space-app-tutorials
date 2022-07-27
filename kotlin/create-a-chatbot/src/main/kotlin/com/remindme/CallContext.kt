package com.remindme

import space.jetbrains.api.runtime.helpers.userId
import space.jetbrains.api.runtime.types.ApplicationPayload

/**
 * This is an example of how you can organize the data used during the call processing.
 * You can put more data in here, for example, user information.
 */
class CallContext(
    val userId: String
)

fun getCallContext(payload: ApplicationPayload): CallContext {
    return CallContext(
        userId = payload.userId ?: throw IllegalArgumentException("payload without userId is not expected")
    )
}
