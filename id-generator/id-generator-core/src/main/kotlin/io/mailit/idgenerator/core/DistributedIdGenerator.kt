package io.mailit.idgenerator.core

import io.mailit.idgenerator.api.IdGenerator
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

internal class DistributedIdGenerator(
    private val serverIdProvider: ServerIdProvider,
) : IdGenerator {

    private val idSequence = AtomicReference(IdSequence(0))

    override tailrec fun generateId(): Long {
        val currentSequence = idSequence.get()

        val currentTime = System.currentTimeMillis() - EPOCH_START

        val sequenceValue: Long = if (currentSequence.createdAt < currentTime) { // If sequence was generated in the past...
            if (idSequence.compareAndSet(currentSequence, IdSequence(currentTime))) { // ...it's recreated if it hasn't been created by another thread.
                0
            } else { // If it has been changed in another thread, we try generate the id again.
                return generateId()
            }
        } else { // If the sequence wasn't generated in the past.
            val value = currentSequence.next()

            // If its value is greater than max value, we try to generate the id again.
            if (value > MAX_SEQUENCE_VALUE) {
                return generateId()
            }

            // Otherwise acquired sequence value is assigned.
            value
        }

        return (currentTime shl TIMESTAMP_SHIFT) or
            (sequenceValue shl SEQUENCE_SHIFT) or
            serverIdProvider.getServerId().toLong()
    }

    private class IdSequence(
        val createdAt: Long,
    ) {
        private val counter = AtomicLong()

        fun next() = counter.incrementAndGet()
    }

    companion object {
        /*
         * 63 bits id length, the first bit is always zero
         *
         * TODO: revise bits allocation. It must be stabilized before 1.0.0 release.
         * 42 bits - timestamp in ms (~135 years)
         * 10 bits - sequence within one millisecond
         * 11 bits - instance id
         */
        private const val SEQUENCE_BITS = 10
        private const val INSTANCE_ID_BITS = 11

        private const val SEQUENCE_SHIFT = INSTANCE_ID_BITS
        private const val TIMESTAMP_SHIFT = SEQUENCE_SHIFT + SEQUENCE_BITS

        private const val MAX_SEQUENCE_VALUE = 1L shl SEQUENCE_BITS - 1

        private const val EPOCH_START = 1672531200000 // 2023-01-01 00:00:00 UTC
    }
}
