package com.spacewebhooks

import com.typesafe.config.ConfigFactory
import io.ktor.config.*

val config by lazy { ConfigFactory.load() }
