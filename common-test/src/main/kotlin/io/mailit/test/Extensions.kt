package io.mailit.test

import java.time.Duration as JavaDuration
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun String.readResource(): String {
    return {}::class.java.classLoader.getResource(this)?.readText()
        ?: throw Exception("Resource: $this is not found")
}

fun nowWithoutNanos(): Instant {
    val now = Instant.now()
    return now.minusNanos(now.nano.toLong())
}

operator fun Instant.plus(duration: Duration): Instant = plus(duration.toJavaDuration())

fun Instant.within(difference: Duration, of: Instant): Boolean {
    val actualDifference = JavaDuration.between(this, of).abs()
    return actualDifference <= difference.toJavaDuration()
}
