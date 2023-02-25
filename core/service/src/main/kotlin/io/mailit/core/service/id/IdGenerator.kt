package io.mailit.core.service.id

fun interface IdGenerator {

    fun generateId(): Long
}
