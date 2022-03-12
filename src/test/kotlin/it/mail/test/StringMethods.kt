package it.mail.test

import java.util.UUID

fun String.isUuid(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (ignored: java.lang.IllegalArgumentException) {
        false
    }
}
