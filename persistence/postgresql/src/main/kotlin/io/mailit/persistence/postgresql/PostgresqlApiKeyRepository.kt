package io.mailit.persistence.postgresql

import io.mailit.core.model.application.ApiKey
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.persistence.common.toLocalDateTime
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple

private const val FIND_BY_ID_SQL = """
    SELECT api.api_key_id api_api_key_id,
           api.name api_name,
           api.secret api_secret,
           api.application_id api_application_id,
           api.expires_at api_expires_at,
           app.application_id app_application_id,
           app.name app_name,
           app.state app_state
      FROM api_key api
     INNER JOIN application app ON app.application_id = api.application_id
     WHERE api.api_key_id = $1"""

private const val FIND_ALL_SQL = """
    SELECT api.api_key_id api_api_key_id,
           api.name api_name,
           api.secret api_secret,
           api.application_id api_application_id,
           api.expires_at api_expires_at,
           app.application_id app_application_id,
           app.name app_name,
           app.state app_state
      FROM api_key api
     INNER JOIN application app ON app.application_id = api.application_id
     ORDER BY api.api_key_id DESC"""

private const val INSERT_SQL = """
    INSERT INTO api_key(
        api_key_id,
        name,
        secret,
        application_id,
        expires_at)
    VALUES($1, $2, $3, $4, $5)"""

private const val DELETE_SQL = "DELETE FROM api_key WHERE api_key_id = $1 AND application_id = $2"

class PostgresqlApiKeyRepository(
    private val client: PgPool,
) : ApiKeyRepository {

    override suspend fun findById(id: String) =
        client.preparedQuery(FIND_BY_ID_SQL)
            .execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getApiKeyFromRow() else null }
            .awaitSuspending()

    override suspend fun findAll(applicationId: Long) =
        client.query(FIND_ALL_SQL)
            .execute()
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getApiKeyFromRow() }
            .collect().asList()
            .awaitSuspending()

    override suspend fun create(apiKey: ApiKey) {
        client.preparedQuery(INSERT_SQL)
            .execute(Tuple.of(apiKey.id, apiKey.name, apiKey.secret, apiKey.application.id, apiKey.expiresAt.toLocalDateTime()))
            .awaitSuspending()
    }

    override suspend fun delete(applicationId: Long, id: String): Boolean =
        client.preparedQuery(DELETE_SQL)
            .execute(Tuple.of(id, applicationId))
            .onItem().transform { it.rowCount() > 0 }
            .awaitSuspending()
}
