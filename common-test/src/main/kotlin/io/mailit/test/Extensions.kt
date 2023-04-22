package io.mailit.test

import java.time.Instant

fun String.readResource(): String {
    return {}::class.java.classLoader.getResource(this)?.readText()
        ?: throw Exception("Resource: $this is not found")
}

fun nowWithoutNanos(): Instant {
    val now = Instant.now()
    return now.minusNanos(now.nano.toLong())
}
