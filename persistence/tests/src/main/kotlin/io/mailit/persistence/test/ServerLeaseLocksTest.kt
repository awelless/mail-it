package io.mailit.persistence.test

import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.ZERO
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

abstract class ServerLeaseLocksTest {

    private val serverId = 15

    @Inject
    lateinit var serverLeaseLocks: ServerLeaseLocks

    @Test
    fun acquireLock_completelyNew_acquires() = runTest {
        // when
        val acquiredFirstTime = serverLeaseLocks.acquireLock(
            serverId = serverId,
            duration = 1.minutes,
            identityKey = "1",
        )

        val acquiredSecondTime = serverLeaseLocks.acquireLock(
            serverId = serverId,
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
        val initialIdentityKey = "initial"

        serverLeaseLocks.acquireLock(
            serverId = serverId,
            duration = ZERO,
            identityKey = initialIdentityKey,
        )

        // when
        delay(1.seconds) // Wait for the lock to expire.

        val acquired = serverLeaseLocks.acquireLock(
            serverId = serverId,
            duration = 1.minutes,
            identityKey = "111",
        )

        val initialProlonged = serverLeaseLocks.prolongLock(
            serverId = serverId,
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
                .map { async { serverLeaseLocks.acquireLock(serverId, 1.minutes, it.toString()) } }
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

        serverLeaseLocks.acquireLock(serverId, initialLockDuration, identityKey)

        // when
        val prolonged = serverLeaseLocks.prolongLock(serverId, 1.seconds, identityKey)

        delay(initialLockDuration) // wait for initial lock duration

        val acquiredByOther = serverLeaseLocks.acquireLock(serverId, 1.seconds, "another-identity") // try to acquire this lock

        // then
        assertTrue(prolonged)
        assertFalse(acquiredByOther)
    }

    @Test
    fun prolongLock_whenAcquiredButExpired_prolongs() = runBlocking {
        // given
        val initialLockDuration = 200.milliseconds
        val identityKey = "123"

        serverLeaseLocks.acquireLock(serverId, initialLockDuration, identityKey)

        // when
        delay(initialLockDuration * 2) // wait for initial lock duration

        val prolonged = serverLeaseLocks.prolongLock(serverId, 1.seconds, identityKey)

        val acquiredByOther = serverLeaseLocks.acquireLock(serverId, 1.seconds, "another-identity") // try to acquire this lock

        // then
        assertTrue(prolonged)
        assertFalse(acquiredByOther)
    }

    @Test
    fun prolongLock_whenAcquiredByOther_doesNothing() = runTest {
        // given
        val identityKey = "123"

        serverLeaseLocks.acquireLock(serverId, 1.minutes, identityKey)

        // when
        val prolonged = serverLeaseLocks.prolongLock(serverId, 1.minutes, "1")

        val prolongedByOwner = serverLeaseLocks.prolongLock(serverId, 1.minutes, identityKey)

        // then
        assertFalse(prolonged)
        assertTrue(prolongedByOwner)
    }

    @Test
    fun prolongLock_whenNotAcquired_doesNothing() = runTest {
        // when
        val prolonged = serverLeaseLocks.prolongLock(serverId, 1.minutes, "1")

        // then
        assertFalse(prolonged)
    }

    @Test
    fun releaseLock_whenAcquired_releases() = runTest {
        // given
        val identityKey = "123"

        serverLeaseLocks.acquireLock(serverId, 1.minutes, identityKey)

        // when
        val released = serverLeaseLocks.releaseLock(serverId, identityKey)

        val acquiredByOther = serverLeaseLocks.acquireLock(serverId, 1.minutes, "another-identity")

        // then
        assertTrue(released)
        assertTrue(acquiredByOther)
    }

    @Test
    fun releaseLock_whenAcquiredByOther_doesNothing() = runTest {
        // given
        val identityKey = "123"

        serverLeaseLocks.acquireLock(serverId, 1.minutes, identityKey)

        // when
        val released = serverLeaseLocks.releaseLock(serverId, "1")

        val reacquired = serverLeaseLocks.acquireLock(serverId, 1.minutes, "2")

        // then
        assertFalse(released)
        assertFalse(reacquired)
    }

    @Test
    fun releaseLock_whenNotAcquired_doesNothing() = runTest {
        // when
        val released = serverLeaseLocks.releaseLock(serverId, "some")

        // then
        assertFalse(released)
    }
}
