package io.mailit.core.spi.id

import kotlin.time.Duration

/**
 * Interface that acts as a collection of locks for a set of application instance ids */
interface InstanceIdLocks {

    /**
     * Tries to acquire lock on [instanceId] for specified [duration]. * [identityKey] is used to determine an owner of acquired lock. It's used for lock prolongation and release *
     * Returns:
     * - true - if lock has been acquired successfully
     * - false - if lock hasn't been acquired
     */
    suspend fun acquireLock(instanceId: Int, duration: Duration, identityKey: String): Boolean

    /**
     * Prolongs lock on [instanceId] with new [duration] marked by [identityKey].
     *
     * Returns:
     * - true - if lock has been prolonged with new [duration]
     * - false - if lock is acquired under another [identityKey] or not acquired at all, therefore, it hasn't been prolonged
     */
    suspend fun prolongLock(instanceId: Int, duration: Duration, identityKey: String): Boolean

    /**
     * Releases lock on [instanceId] marked by [identityKey].
     * * Returns:
     * - true - if lock has been released or didn't exist before
     * - false - if lock is acquired under another [identityKey], therefore, it hasn't been released
     */
    suspend fun releaseLock(instanceId: Int, identityKey: String): Boolean
}
