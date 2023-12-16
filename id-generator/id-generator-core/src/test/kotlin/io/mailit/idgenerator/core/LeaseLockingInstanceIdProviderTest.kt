package io.mailit.idgenerator.core

import io.mailit.idgenerator.fake.InMemoryServerLeaseLocks
import io.mockk.coVerify
import io.mockk.spyk
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class LeaseLockingInstanceIdProviderTest {

    @Test
    fun `2048 instances - every has unique instance id`() = runTest {
        // given
        val locks = InMemoryServerLeaseLocks()

        val expectedInstanceIds = (0..MAX_INSTANCE_ID).toSet()

        val providers = expectedInstanceIds
            .map {
                LeaseLockingServerIdProvider(
                    serverLeaseLocks = locks,
                    lockProlongationCoroutineContext = Dispatchers.Default,
                    lockDuration = 15.minutes,
                    prolongationDelay = 5.minutes,
                )
            }

        // when
        val actualServerIds = withContext(Dispatchers.Default) {
            providers
                .map {
                    async {
                        it.initialize()
                        it.getServerId()
                    }
                }
                .awaitAll()
                .toSet()
        }

        withContext(Dispatchers.Default) {
            providers
                .map { async { it.stop() } }
                .awaitAll()
        }

        // then
        assertEquals(expectedInstanceIds, actualServerIds)
    }

    @Test
    fun stopWithoutInvocation_noExceptionIsThrown() = runTest {
        // given
        val locks = InMemoryServerLeaseLocks()

        val provider = LeaseLockingServerIdProvider(
            serverLeaseLocks = locks,
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
        val locks: InMemoryServerLeaseLocks = spyk()

        val lockDuration = 1.seconds
        val prolongationDelay = lockDuration / 4

        val provider = LeaseLockingServerIdProvider(
            serverLeaseLocks = locks,
            lockProlongationCoroutineContext = Dispatchers.Default,
            lockDuration = lockDuration,
            prolongationDelay = prolongationDelay,
        )

        // when
        provider.initialize() // trigger lock acquisition and prolongation
        val serverId = provider.getServerId()

        val awaitDelay = lockDuration * 2 // await excessive amount of time, so lock can be prolonged multiple times
        delay(awaitDelay)

        val acquired = locks.acquireLock(serverId, 1.seconds, "test") // try to acquire lock

        provider.stop()

        // then
        assertFalse(acquired)

        val expectedProlongationInvocations = (awaitDelay / prolongationDelay).toInt()
        coVerify(atLeast = expectedProlongationInvocations - 1) { locks.prolongLock(eq(serverId), any(), any()) }
    }

    companion object {
        private const val MAX_INSTANCE_ID = 2047
    }
}
