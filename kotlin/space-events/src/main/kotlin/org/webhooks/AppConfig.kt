package org.webhooks

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

val config: Config by lazy { ConfigFactory.load() }
