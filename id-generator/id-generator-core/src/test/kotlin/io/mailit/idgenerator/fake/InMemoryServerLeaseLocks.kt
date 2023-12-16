package io.mailit.idgenerator.fake

import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class InMemoryServerLeaseLocks : ServerLeaseLocks {

    private val serverIds = ConcurrentHashMap<Int, LockData>()

    override suspend fun acquireLock(serverId: Int, duration: Duration, identityKey: String): Boolean {
        val now = Instant.now()
        val lockData = LockData(
            identityKey = identityKey,
            acquiredTill = now.plus(duration.toJavaDuration()),
        )

        val actual = serverIds.compute(serverId) { _, existingLockData ->
            if (existingLockData == null || existingLockData.acquiredTill.isBefore(now)) {
                lockData
            } else {
                existingLockData
            }
        }

        return actual === lockData
    }

    override suspend fun prolongLock(serverId: Int, duration: Duration, identityKey: String): Boolean {
        val now = Instant.now()
        val lockData = LockData(
            identityKey = identityKey,
            acquiredTill = now.plus(duration.toJavaDuration()),
        )

        val actual = serverIds.computeIfPresent(serverId) { _, existingLockData ->
            if (existingLockData.identityKey == identityKey) {
                lockData
            } else {
                existingLockData
            }
        }

        return actual === lockData
    }

    override suspend fun releaseLock(serverId: Int, identityKey: String): Boolean {
        val actual = serverIds.computeIfPresent(serverId) { _, existingLockData ->
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
