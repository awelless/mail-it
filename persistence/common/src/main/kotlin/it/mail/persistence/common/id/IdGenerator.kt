package it.mail.persistence.common.id

import java.util.concurrent.atomic.AtomicLong

fun interface IdGenerator {

    fun generateId(): Long
}

class InMemoryIdGenerator : IdGenerator {

    private val counter = AtomicLong()

    override fun generateId(): Long = counter.incrementAndGet()
}
