package io.mailit.persistence.postgresql

import io.mailit.core.model.application.ApiKey
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.postgresql.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.postgresql.Columns.Application as ApplicationCol
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple

private const val FIND_BY_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT},
           app.application_id ${ApplicationCol.ID},
           app.name ${ApplicationCol.NAME},
           app.state ${ApplicationCol.STATE}
      FROM api_key api
     INNER JOIN application app ON app.application_id = api.application_id
     WHERE api.api_key_id = $1"""

private const val FIND_ALL_BY_APPLICATION_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT},
           app.application_id ${ApplicationCol.ID},
           app.name ${ApplicationCol.NAME},
           app.state ${ApplicationCol.STATE}
      FROM api_key api
     INNER JOIN application app ON app.application_id = api.application_id
     WHERE app.application_id = $1
     ORDER BY api.created_at DESC"""

private const val INSERT_SQL = """
    INSERT INTO api_key(
        api_key_id,
        name,
        secret,
        application_id,
        created_at,
        expires_at)
    VALUES($1, $2, $3, $4, $5, $6)"""

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

    override suspend fun findAllByApplicationId(applicationId: Long): List<ApiKey> =
        client.preparedQuery(FIND_ALL_BY_APPLICATION_ID_SQL)
            .execute(Tuple.of(applicationId))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getApiKeyFromRow() }
            .collect().asList()
            .awaitSuspending()

    override suspend fun create(apiKey: ApiKey) {
        val parameters = Tuple.of(
            apiKey.id,
            apiKey.name,
            apiKey.secret,
            apiKey.application.id,
            apiKey.createdAt.toLocalDateTime(),
            apiKey.expiresAt.toLocalDateTime(),
        )

        client.preparedQuery(INSERT_SQL)
            .execute(parameters)
            .awaitSuspending()
    }

    override suspend fun delete(applicationId: Long, id: String): Boolean =
        client.preparedQuery(DELETE_SQL)
            .execute(Tuple.of(id, applicationId))
            .onItem().transform { it.rowCount() > 0 }
            .awaitSuspending()
}
