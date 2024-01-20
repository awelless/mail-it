package io.mailit.apikey.lang

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

operator fun Instant.minus(duration: Duration): Instant = minus(duration.toJavaDuration())
operator fun Instant.plus(duration: Duration): Instant = plus(duration.toJavaDuration())
