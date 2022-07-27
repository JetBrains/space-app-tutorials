package org.webhooks

import space.jetbrains.api.runtime.Batch

fun Batch<*>.hasNext() = data.isNotEmpty()
