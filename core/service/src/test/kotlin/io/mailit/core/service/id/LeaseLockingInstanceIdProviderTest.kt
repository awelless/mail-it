package io.mailit.core.service.id

import io.mailit.core.spi.id.InstanceIdLocks
import io.mockk.coVerify
import io.mockk.spyk
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class LeaseLockingInstanceIdProviderTest {

    @Test
    fun `2048 instances - every has unique instance id`() = runTest {
        // given
        val locks = InMemoryInstanceIdLocks()

        val expectedInstanceIds = (0..MAX_INSTANCE_ID).toSet()

        val providers = expectedInstanceIds
            .map {
                LeaseLockingInstanceIdProvider(
                    instanceIdLocks = locks,
                    lockProlongationCoroutineContext = Dispatchers.Default,
                    lockDuration = 15.minutes,
                    prolongationDelay = 5.minutes,
                )
            }

        // when
        val actualInstanceIds = withContext(Dispatchers.Default) {
            val instanceIds = providers
                .map {
                    async {
                        it.initialize()
                        it.getInstanceId()
                    }
                }
                .awaitAll()
                .toSet()

            providers
                .map { async { it.stop() } }
                .awaitAll()

            instanceIds
        }

        // then
        assertEquals(expectedInstanceIds, actualInstanceIds)
    }

    @Test
    fun stopWithoutInvocation_noExceptionIsThrown() = runTest {
        // given
        val locks = InMemoryInstanceIdLocks()

        val provider = LeaseLockingInstanceIdProvider(
            instanceIdLocks = locks,
            lockProlongationCoroutineContext = Dispatchers.Default,
            lockDuration = 15.minutes,
            prolongationDelay = 5.minutes,
        )

        // when + then
        assertDoesNotThrow { provider.stop() }
    }

    @Test
    fun prolongation_whenOnTime_prolongsSuccessfully() = runBlocking {
        // given
        val locks: InMemoryInstanceIdLocks = spyk()

        val lockDuration = 1.seconds
        val prolongationDelay = lockDuration / 4

        val provider = LeaseLockingInstanceIdProvider(
            instanceIdLocks = locks,
            lockProlongationCoroutineContext = Dispatchers.Default,
            lockDuration = lockDuration,
            prolongationDelay = prolongationDelay,
        )

        // when
        provider.initialize() // trigger lock acquisition and prolongation
        val instanceId = provider.getInstanceId()

        val awaitDelay = lockDuration * 2 // await excessive amount of time, so lock can be prolonged multiple times
        delay(awaitDelay)

        val acquired = locks.acquireLock(instanceId, 1.seconds, "test") // try to acquire lock

        provider.stop()

        // then
        assertFalse(acquired)

        val expectedProlongationInvocations = (awaitDelay / prolongationDelay).toInt()
        coVerify(atLeast = expectedProlongationInvocations - 1) { locks.prolongLock(eq(instanceId), any(), any()) }
    }

    companion object {
        private const val MAX_INSTANCE_ID = 2047
    }
}

private class InMemoryInstanceIdLocks : InstanceIdLocks {

    private val instanceIds = ConcurrentHashMap<Int, LockData>()

    override suspend fun acquireLock(instanceId: Int, duration: Duration, identityKey: String): Boolean {
        val now = Instant.now()
        val lockData = LockData(
            identityKey = identityKey,
            acquiredTill = now.plus(duration.toJavaDuration()),
        )

        val actual = instanceIds.compute(instanceId) { _, existingLockData ->
            if (existingLockData == null || existingLockData.acquiredTill.isBefore(now)) {
                lockData
            } else {
                existingLockData
            }
        }

        return actual === lockData
    }

    override suspend fun prolongLock(instanceId: Int, duration: Duration, identityKey: String): Boolean {
        val now = Instant.now()
        val lockData = LockData(
            identityKey = identityKey,
            acquiredTill = now.plus(duration.toJavaDuration()),
        )

        val actual = instanceIds.computeIfPresent(instanceId) { _, existingLockData ->
            if (existingLockData.identityKey == identityKey) {
                lockData
            } else {
                existingLockData
            }
        }

        return actual === lockData
    }

    override suspend fun releaseLock(instanceId: Int, identityKey: String): Boolean {
        val actual = instanceIds.computeIfPresent(instanceId) { _, existingLockData ->
            if (existingLockData.identityKey == identityKey) {
                null
            } else {
                existingLockData
            }
        }

        return actual === null
    }

    private data class LockData(
        val identityKey: String,
        val acquiredTill: Instant,
    )
}
