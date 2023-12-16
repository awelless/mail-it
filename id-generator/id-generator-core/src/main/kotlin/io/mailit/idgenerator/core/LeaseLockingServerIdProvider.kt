package io.mailit.idgenerator.core

import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging

internal class LeaseLockingServerIdProvider(
    private val serverLeaseLocks: ServerLeaseLocks,

    lockProlongationCoroutineContext: CoroutineContext,
    private val lockDuration: Duration,
    private val prolongationDelay: Duration,
) : ServerIdProvider {

    private val lockProlongationCoroutineScope = CoroutineScope(lockProlongationCoroutineContext)

    private val identityKey = UUID.randomUUID().toString()
    private var _serverId: Int = NOT_INITIALIZED

    /**
     * A mutex that disallows running [initialize] and [stop] simultaneously.
     */
    private val lifecycleMutex = Mutex()

    @Volatile
    private var stopped = false

    // TODO: check whether _serverId is initialized?
    override fun getServerId() = _serverId

    suspend fun initialize() {
        if (stopped) {
            return
        }

        logger.info { "Acquiring serverId" }

        lifecycleMutex.withLock {
            _serverId = acquireServerId()
            startProlongationLoop()
        }

        logger.info { "Acquired serverId: $_serverId" }
    }

    private tailrec suspend fun acquireServerId(attempt: Int = 0): Int {
        if (attempt == MAX_SERVER_ID_ACQUISITION_ATTEMPTS) {
            logger.error { "Failed to acquire serverId in $MAX_SERVER_ID_ACQUISITION_ATTEMPTS attempts" }
            throw FailedToAcquireServerIdException()
        }

        val serverId = (0..MAX_SERVER_ID)
            .firstOrNull {
                serverLeaseLocks.acquireLock(
                    serverId = it,
                    duration = lockDuration,
                    identityKey = identityKey,
                )
            }

        return serverId ?: acquireServerId(attempt + 1)
    }

    private fun startProlongationLoop() = lockProlongationCoroutineScope.launch {
        do {
            delay(prolongationDelay)

            try {
                val prolonged = serverLeaseLocks.prolongLock(
                    serverId = _serverId,
                    duration = lockDuration,
                    identityKey = identityKey,
                )

                if (!prolonged) {
                    // TODO: check the identityKey for the current serverId and react if it's not the same as expected.
                    logger.warn { "Failed to prolong serverId lock. ServerId: $_serverId, identityKey: $identityKey" }
                }
            } catch (e: Exception) {
                logger.error(e) { "An error during prolonging server lock occurred." }
            }
        } while (isActive)
    }

    suspend fun stop() {
        if (stopped) {
            return
        }

        val released = lifecycleMutex.withLock {
            stopped = true

            if (_serverId == NOT_INITIALIZED) {
                return
            }

            logger.info { "Releasing lock for serverId: $_serverId" }

            if (lockProlongationCoroutineScope.isActive) {
                lockProlongationCoroutineScope.cancel()
            }

            serverLeaseLocks.releaseLock(
                serverId = _serverId,
                identityKey = identityKey,
            )
        }

        if (released) {
            logger.info { "Released lock for serverId: $_serverId" }
        } else {
            logger.warn { "Failed to release serverId lock. ServerId: $_serverId, identityKey: $identityKey" }
        }
    }

    private class FailedToAcquireServerIdException : RuntimeException("Failed to acquire serverId")

    companion object : KLogging() {
        private const val NOT_INITIALIZED = -1

        private const val MAX_SERVER_ID = 2047 // 2^11 - 1

        private const val MAX_SERVER_ID_ACQUISITION_ATTEMPTS = 10
    }
}
