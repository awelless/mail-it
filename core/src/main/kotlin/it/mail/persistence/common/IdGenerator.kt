package it.mail.persistence.common

import java.util.concurrent.atomic.AtomicLong

interface IdGenerator {

    fun generateId(): Long
}

class LocalCounterIdGenerator : IdGenerator {

    private val counter = AtomicLong()

    override fun generateId(): Long = counter.incrementAndGet()
}

class SingleInstanceIdGenerator : IdGenerator {

    override fun generateId(): Long {
        TODO("Not yet implemented")
    }
}
