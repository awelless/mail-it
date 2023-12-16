package io.mailit.idgenerator.spi.locking

import kotlin.time.Duration

/**
 * Interface that acts as a collection of lease locks on a set of application servers in the cluster.
 * The locks can be acquired for limited time only.
 */
interface ServerLeaseLocks {

    /**
     * Tries to acquire lock on [serverId] for specified [duration].
     * [identityKey] is used to determine an owner of the acquired lock. Each application server instance must have its own unique [identityKey].
     *
     * Returns, whether the lock has been acquired successfully.
     */
    suspend fun acquireLock(serverId: Int, duration: Duration, identityKey: String): Boolean

    /**
     * Prolongs a lock on [serverId] with new [duration] marked by [identityKey].
     *
     * Returns:
     * - true - if the lock has been prolonged with new [duration];
     * - false - if the lock is acquired under another [identityKey] or not acquired at all, therefore, it hasn't been prolonged.
     */
    suspend fun prolongLock(serverId: Int, duration: Duration, identityKey: String): Boolean

    /**
     * Releases a lock on [serverId] marked by [identityKey].
     *
     * Returns:
     * - true - if the lock has been released or didn't exist before;
     * - false - if the lock is acquired under another [identityKey], therefore, it hasn't been released.
     */
    suspend fun releaseLock(serverId: Int, identityKey: String): Boolean
}
