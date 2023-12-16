package io.mailit.persistence.mysql

import io.mailit.idgenerator.spi.locking.ServerLeaseLocks
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.mysql.Tables.SERVER_LEASE_LOCKS
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.mysqlclient.MySQLPool
import io.vertx.mutiny.sqlclient.Tuple
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import mu.KLogging

private const val ACQUIRE_LOCK = """
    INSERT INTO $SERVER_LEASE_LOCKS(
        server_id,
        acquired_until,
        identity_key
    ) VALUES (?, ?, ?)
"""

private const val CLEANUP_EXPIRED_LOCK = """
    DELETE FROM $SERVER_LEASE_LOCKS
     WHERE server_id = ?
       AND acquired_until < ?
"""

private const val PROLONG_LOCK = """
    UPDATE $SERVER_LEASE_LOCKS
       SET acquired_until = ?
     WHERE server_id = ?
       AND identity_key = ?
"""

private const val RELEASE_LOCK = """
    DELETE FROM $SERVER_LEASE_LOCKS 
     WHERE server_id = ? 
       AND identity_key = ?
"""

class MysqlServerLeaseLocks(
    private val client: MySQLPool,
) : ServerLeaseLocks {

    override suspend fun acquireLock(serverId: Int, duration: Duration, identityKey: String): Boolean = client
        .withTransaction { connection ->
            val now = Instant.now()
            connection.preparedQuery(CLEANUP_EXPIRED_LOCK).execute(Tuple.of(serverId, now.toLocalDateTime()))
                .onItem().transformToUni { _ ->
                    val acquiredUntil = now.plus(duration.toJavaDuration()).toLocalDateTime()
                    connection.preparedQuery(ACQUIRE_LOCK).execute(Tuple.of(serverId, acquiredUntil, identityKey))
                }
        }
        .onItem().transform { true }
        .onFailure().recoverWithItem { e ->
            logger.debug(e) { "Failed to acquire lock for instance: $serverId" }
            false
        }
        .awaitSuspending()

    override suspend fun prolongLock(serverId: Int, duration: Duration, identityKey: String): Boolean = client
        .withTransaction { connection ->
            val acquiredUntil = Instant.now().plus(duration.toJavaDuration()).toLocalDateTime()
            connection.preparedQuery(PROLONG_LOCK).execute(Tuple.of(acquiredUntil, serverId, identityKey))
        }
        .onItem().transform { it.rowCount() > 0 }
        .onFailure().recoverWithItem { e ->
            logger.warn(e) { "Failed to prolong lock for instance: $serverId with identity key $identityKey" }
            false
        }
        .awaitSuspending()

    override suspend fun releaseLock(serverId: Int, identityKey: String): Boolean = client
        .withTransaction { connection ->
            connection.preparedQuery(RELEASE_LOCK).execute(Tuple.of(serverId, identityKey))
        }
        .onItem().transform { it.rowCount() > 0 }
        .onFailure().recoverWithItem { e ->
            logger.warn(e) { "Failed to release lock for instance: $serverId with identity key $identityKey" }
            false
        }
        .awaitSuspending()

    companion object : KLogging()
}
