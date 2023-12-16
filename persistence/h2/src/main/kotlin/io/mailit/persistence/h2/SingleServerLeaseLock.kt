package io.mailit.persistence.h2

import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SingleServerLeaseLock : ServerLeaseLocks {

    private var identityKey: String? = null
    private var acquiredUntil: Instant? = null

    private val mutex = Mutex()

    override suspend fun acquireLock(serverId: Int, duration: Duration, identityKey: String) = mutex.withLock {
        val now = Instant.now()

        if (serverId != 0 || this.acquiredUntil?.isAfter(now) == true) {
            false
        } else {
            this.identityKey = identityKey
            this.acquiredUntil = now.plus(duration.toJavaDuration())
            true
        }
    }

    override suspend fun prolongLock(serverId: Int, duration: Duration, identityKey: String) = mutex.withLock {
        if (serverId != 0 || this.identityKey != identityKey) {
            false
        } else {
            this.acquiredUntil = Instant.now().plus(duration.toJavaDuration())
            true
        }
    }

    override suspend fun releaseLock(serverId: Int, identityKey: String) = mutex.withLock {
        if (serverId != 0 || this.identityKey != identityKey) {
            false
        } else {
            this.identityKey = null
            this.acquiredUntil = null
            true
        }
    }
}
