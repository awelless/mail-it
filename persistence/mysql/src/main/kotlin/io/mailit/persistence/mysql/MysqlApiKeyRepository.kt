package io.mailit.persistence.mysql

import io.mailit.core.model.ApiKey
import io.mailit.core.spi.ApiKeyRepository
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.mysql.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.mysql.Tables.API_KEY
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.mysqlclient.MySQLPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.mysqlclient.MySQLException

private const val FIND_BY_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT}
      FROM $API_KEY api
     WHERE api.api_key_id = ?"""

private const val FIND_ALL_BY_APPLICATION_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT}
      FROM $API_KEY api
     ORDER BY api.created_at DESC"""

private const val INSERT_SQL = """
    INSERT INTO $API_KEY(
        api_key_id,
        name,
        secret,
        created_at,
        expires_at)
    VALUES(?, ?, ?, ?, ?)"""

private const val DELETE_SQL = "DELETE FROM $API_KEY WHERE api_key_id = ?"

class MysqlApiKeyRepository(
    private val client: MySQLPool,
) : ApiKeyRepository {

    override suspend fun findById(id: String) =
        client.preparedQuery(FIND_BY_ID_SQL)
            .execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getApiKeyFromRow() else null }
            .awaitSuspending()

    override suspend fun findAll(): List<ApiKey> =
        client.preparedQuery(FIND_ALL_BY_APPLICATION_ID_SQL)
            .execute()
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getApiKeyFromRow() }
            .collect().asList()
            .awaitSuspending()

    override suspend fun create(apiKey: ApiKey) {
        val parameters = Tuple.of(
            apiKey.id,
            apiKey.name,
            apiKey.secret,
            apiKey.createdAt.toLocalDateTime(),
            apiKey.expiresAt.toLocalDateTime(),
        )

        client.preparedQuery(INSERT_SQL)
            .execute(parameters)
            .onFailure(MySQLException::class.java).transform {
                if ((it as? MySQLException)?.errorCode == 1062) DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()
    }

    override suspend fun delete(id: String): Boolean =
        client.preparedQuery(DELETE_SQL)
            .execute(Tuple.of(id))
            .onItem().transform { it.rowCount() > 0 }
            .awaitSuspending()
}
