package io.mailit.persistence.postgresql

import io.mailit.core.spi.id.InstanceIdLocks
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.postgresql.Tables.INSTANCE_ID_LOCKS
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import mu.KLogging

private const val ACQUIRE_LOCK = """
    INSERT INTO $INSTANCE_ID_LOCKS(
        instance_id,
        acquired_until,
        identity_key
    ) VALUES ($1, $2, $3)
"""

private const val CLEANUP_EXPIRED_LOCK = """
    DELETE FROM $INSTANCE_ID_LOCKS
     WHERE instance_id = $1
       AND acquired_until < $2
"""

private const val PROLONG_LOCK = """
    UPDATE $INSTANCE_ID_LOCKS
       SET acquired_until = $1
     WHERE instance_id = $2
       AND identity_key = $3
"""

private const val RELEASE_LOCK = """
    DELETE FROM $INSTANCE_ID_LOCKS 
     WHERE instance_id = $1 
       AND identity_key = $2
"""

class PostgresqlInstanceIdLocks(
    private val client: PgPool,
) : InstanceIdLocks {

    override suspend fun acquireLock(instanceId: Int, duration: Duration, identityKey: String): Boolean = client
        .withTransaction { connection ->
            val now = Instant.now()
            connection.preparedQuery(CLEANUP_EXPIRED_LOCK).execute(Tuple.of(instanceId, now.toLocalDateTime()))
                .onItem().transformToUni { _ ->
                    val acquiredUntil = now.plus(duration.toJavaDuration()).toLocalDateTime()
                    connection.preparedQuery(ACQUIRE_LOCK).execute(Tuple.of(instanceId, acquiredUntil, identityKey))
                }
        }
        .onItem().transform { true }
        .onFailure().recoverWithItem { e ->
            logger.debug(e) { "Failed to acquire lock for instance: $instanceId" }
            false
        }
        .awaitSuspending()

    override suspend fun prolongLock(instanceId: Int, duration: Duration, identityKey: String): Boolean = client
        .withTransaction { connection ->
            val acquiredUntil = Instant.now().plus(duration.toJavaDuration()).toLocalDateTime()
            connection.preparedQuery(PROLONG_LOCK).execute(Tuple.of(acquiredUntil, instanceId, identityKey))
        }
        .onItem().transform { it.rowCount() > 0 }
        .onFailure().recoverWithItem { e ->
            logger.warn(e) { "Failed to prolong lock for instance: $instanceId with identity key $identityKey" }
            false
        }
        .awaitSuspending()

    override suspend fun releaseLock(instanceId: Int, identityKey: String): Boolean = client
        .withTransaction { connection ->
            connection.preparedQuery(RELEASE_LOCK).execute(Tuple.of(instanceId, identityKey))
        }
        .onItem().transform { it.rowCount() > 0 }
        .onFailure().recoverWithItem { e ->
            logger.warn(e) { "Failed to release lock for instance: $instanceId with identity key $identityKey" }
            false
        }
        .awaitSuspending()

    companion object : KLogging()
}
