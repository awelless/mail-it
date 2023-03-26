package io.mailit.core.service.id

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class DistributedIdGenerator(
    private val instanceIdProvider: InstanceIdProvider,
) : IdGenerator {

    companion object {
        /*
         * 63 bits id length, the first bit is always zero
         *
         * todo revise bits allocation. should be stabilized before 1.0.0 release
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

    private val idSequence = AtomicReference(IdSequence(0))

    override tailrec fun generateId(): Long {
        val currentSequence = idSequence.get()

        val currentTime = System.currentTimeMillis() - EPOCH_START

        val sequenceValue: Long = if (currentSequence.createdAt < currentTime) { // if sequence was generated in the past
            if (idSequence.compareAndSet(currentSequence, IdSequence(currentTime))) { // it's recreated if it's still not changed by another thread
                0
            } else { // if it was changed in another thread, we try generate id again
                return generateId()
            }
        } else { // if sequence wasn't generated in the past
            val value = currentSequence.next()

            // if it's value is greater than max value, we try to generate  id again
            if (value > MAX_SEQUENCE_VALUE) {
                return generateId()
            }

            // otherwise acquired sequence value is assigned
            value
        }

        return (currentTime shl TIMESTAMP_SHIFT) or
            (sequenceValue shl SEQUENCE_SHIFT) or
            instanceIdProvider.getInstanceId().toLong()
    }

    private class IdSequence(
        val createdAt: Long,
    ) {
        private val counter = AtomicLong()

        fun next() = counter.incrementAndGet()
    }
}
