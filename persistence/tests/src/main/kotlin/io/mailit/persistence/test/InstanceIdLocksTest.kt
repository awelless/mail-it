package io.mailit.persistence.test

import io.mailit.core.spi.id.InstanceIdLocks
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
abstract class InstanceIdLocksTest {

    private val instanceId = 15

    @Inject
    lateinit var instanceIdLocks: InstanceIdLocks

    @Test
    fun acquireLock_completelyNew_acquires() = runTest {
        // when
        val acquiredFirstTime = instanceIdLocks.acquireLock(
            instanceId = instanceId,
            duration = 1.minutes,
            identityKey = "1",
        )

        val acquiredSecondTime = instanceIdLocks.acquireLock(
            instanceId = instanceId,
            duration = 1.minutes,
            identityKey = "2",
        )

        // then
        assertTrue(acquiredFirstTime)
        assertFalse(acquiredSecondTime)
    }

    @Test
    fun acquireLock_expired_acquires() = runBlocking {
        // given
        val initialLockDuration = 200.milliseconds
        val initialIdentityKey = "initial"

        instanceIdLocks.acquireLock(
            instanceId = instanceId,
            duration = initialLockDuration,
            identityKey = initialIdentityKey,
        )

        // when
        delay(initialLockDuration * 2) // wait for lock to expire

        val acquired = instanceIdLocks.acquireLock(
            instanceId = instanceId,
            duration = 1.minutes,
            identityKey = "111",
        )

        val initialProlonged = instanceIdLocks.prolongLock(
            instanceId = instanceId,
            duration = 1.minutes,
            identityKey = initialIdentityKey,
        )

        // then
        assertTrue(acquired)
        assertFalse(initialProlonged)
    }

    @Test
    fun acquireLock_highConcurrency() = runTest {
        val acquisitionsCount = withContext(Dispatchers.Default) {
            val attempts = 100

            (1..attempts)
                .map { async { instanceIdLocks.acquireLock(instanceId, 1.minutes, it.toString()) } }
                .awaitAll()
                .count { it }
        }

        assertEquals(1, acquisitionsCount)
    }

    @Test
    fun prolongLock_whenAcquired_prolongs() = runBlocking {
        // given
        val initialLockDuration = 200.milliseconds
        val identityKey = "123"

        instanceIdLocks.acquireLock(instanceId, initialLockDuration, identityKey)

        // when
        val prolonged = instanceIdLocks.prolongLock(instanceId, 1.seconds, identityKey)

        delay(initialLockDuration) // wait for initial lock duration

        val acquiredByOther = instanceIdLocks.acquireLock(instanceId, 1.seconds, "another-identity") // try to acquire this lock

        // then
        assertTrue(prolonged)
        assertFalse(acquiredByOther)
    }

    @Test
    fun prolongLock_whenAcquiredButExpired_prolongs() = runBlocking {
        // given
        val initialLockDuration = 200.milliseconds
        val identityKey = "123"

        instanceIdLocks.acquireLock(instanceId, initialLockDuration, identityKey)

        // when
        delay(initialLockDuration * 2) // wait for initial lock duration

        val prolonged = instanceIdLocks.prolongLock(instanceId, 1.seconds, identityKey)

        val acquiredByOther = instanceIdLocks.acquireLock(instanceId, 1.seconds, "another-identity") // try to acquire this lock

        // then
        assertTrue(prolonged)
        assertFalse(acquiredByOther)
    }

    @Test
    fun prolongLock_whenAcquiredByOther_doesNothing() = runTest {
        // given
        val identityKey = "123"

        instanceIdLocks.acquireLock(instanceId, 1.minutes, identityKey)

        // when
        val prolonged = instanceIdLocks.prolongLock(instanceId, 1.minutes, "1")

        val prolongedByOwner = instanceIdLocks.prolongLock(instanceId, 1.minutes, identityKey)

        // then
        assertFalse(prolonged)
        assertTrue(prolongedByOwner)
    }

    @Test
    fun prolongLock_whenNotAcquired_doesNothing() = runTest {
        // when
        val prolonged = instanceIdLocks.prolongLock(instanceId, 1.minutes, "1")

        // then
        assertFalse(prolonged)
    }

    @Test
    fun releaseLock_whenAcquired_releases() = runTest {
        // given
        val identityKey = "123"

        instanceIdLocks.acquireLock(instanceId, 1.minutes, identityKey)

        // when
        val released = instanceIdLocks.releaseLock(instanceId, identityKey)

        val acquiredByOther = instanceIdLocks.acquireLock(instanceId, 1.minutes, "another-identity")

        // then
        assertTrue(released)
        assertTrue(acquiredByOther)
    }

    @Test
    fun releaseLock_whenAcquiredByOther_doesNothing() = runTest {
        // given
        val identityKey = "123"

        instanceIdLocks.acquireLock(instanceId, 1.minutes, identityKey)

        // when
        val released = instanceIdLocks.releaseLock(instanceId, "1")

        val reacquired = instanceIdLocks.acquireLock(instanceId, 1.minutes, "2")

        // then
        assertFalse(released)
        assertFalse(reacquired)
    }

    @Test
    fun releaseLock_whenNotAcquired_doesNothing() = runTest {
        // when
        val released = instanceIdLocks.releaseLock(instanceId, "some")

        // then
        assertFalse(released)
    }
}
