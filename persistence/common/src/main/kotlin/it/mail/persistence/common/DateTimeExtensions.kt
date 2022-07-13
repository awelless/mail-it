package it.mail.persistence.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, UTC)
