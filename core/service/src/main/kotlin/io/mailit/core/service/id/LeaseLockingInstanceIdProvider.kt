package io.mailit.core.service.id

import io.mailit.core.spi.id.InstanceIdLocks
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import mu.KLogging

class LeaseLockingInstanceIdProvider(
    private val instanceIdLocks: InstanceIdLocks,

    lockProlongationCoroutineContext: CoroutineContext,
    private val lockDuration: Duration,
    private val prolongationDelay: Duration,
) : InstanceIdProvider {

    private val lockProlongationCoroutineScope = CoroutineScope(lockProlongationCoroutineContext)

    private val identityKey = UUID.randomUUID().toString()
    private var _instanceId: Int = NOT_INITIALIZED

    /**
     * Lock that disallows to run [initialize] and [stop] simultaneously
     */
    private val lifecycleMutex = Mutex()

    @Volatile
    private var stopped = false

    // check if initialized first?
    override fun getInstanceId() = _instanceId

    suspend fun initialize() {
        try {
            lifecycleMutex.lock()

            if (stopped) {
                return
            }

            logger.info { "Acquiring instanceId" }

            _instanceId = acquireInstanceId()
            startProlongationLoop()

            logger.info { "Acquired instanceId: $_instanceId" }
        } finally {
            lifecycleMutex.unlock()
        }
    }

    private tailrec suspend fun acquireInstanceId(attempt: Int = 0): Int {
        if (attempt == MAX_INSTANCE_ID_ACQUISITION_ATTEMPTS) {
            logger.error { "Failed to acquire instanceId in $MAX_INSTANCE_ID_ACQUISITION_ATTEMPTS attempts" }
            throw FailedToAcquireInstanceIdException()
        }

        val instanceId = runBlocking {
            (0..MAX_INSTANCE_ID)
                .firstOrNull {
                    instanceIdLocks.acquireLock(
                        instanceId = it,
                        duration = lockDuration,
                        identityKey = identityKey,
                    )
                }
        }

        return instanceId ?: acquireInstanceId(attempt + 1)
    }

    private fun startProlongationLoop() {
        lockProlongationCoroutineScope.launch {
            delay(prolongationDelay)

            while (isActive) {
                val prolonged = instanceIdLocks.prolongLock(
                    instanceId = _instanceId,
                    duration = lockDuration,
                    identityKey = identityKey,
                )

                if (prolonged) {
                    delay(prolongationDelay)
                } else {
                    // todo check identityKey of instanceId and react if it's not the same as expected
                    logger.warn { "Failed to prolong instanceId lock. InstanceId: $_instanceId, identityKey: $identityKey" }
                }
            }
        }
    }

    suspend fun stop() {
        try {
            lifecycleMutex.lock()

            if (stopped) {
                return
            }

            stopped = true

            if (_instanceId == NOT_INITIALIZED) {
                return
            }

            logger.info { "Releasing lock for instanceId: $_instanceId" }

            lockProlongationCoroutineScope.cancel()

            val released = instanceIdLocks.releaseLock(
                instanceId = _instanceId,
                identityKey = identityKey,
            )

            if (released) {
                logger.info { "Released lock for instanceId: $_instanceId" }
            } else {
                logger.warn { "Failed to release instanceId lock. InstanceId: $_instanceId, identityKey: $identityKey" }
            }
        } finally {
            lifecycleMutex.unlock()
        }
    }

    private class FailedToAcquireInstanceIdException : RuntimeException("Failed to acquire instanceId")

    companion object : KLogging() {
        private const val NOT_INITIALIZED = -1

        private const val MAX_INSTANCE_ID = 2047 // 2^11 - 1

        private const val MAX_INSTANCE_ID_ACQUISITION_ATTEMPTS = 10
    }
}
