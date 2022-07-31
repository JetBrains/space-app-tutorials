package org.homepage

import space.jetbrains.api.runtime.Space

fun resourceBytes(resourcePath: String): ByteArray {
    val inputStream =
        Space::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: error("Could not read resource $resourcePath")
    return inputStream.use { it.readBytes() }
}

fun resourceFileAsString(resourcePath: String): String {
    val bytes = resourceBytes(resourcePath)
    return String(bytes)
}
