package io.mailit.persistence.h2

import io.mailit.core.model.application.ApiKey
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.persistence.h2.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.h2.Columns.Application as ApplicationCol
import io.mailit.persistence.h2.Tables.API_KEY
import io.mailit.persistence.h2.Tables.APPLICATION
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner

private const val FIND_BY_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT},
           app.application_id ${ApplicationCol.ID},
           app.name ${ApplicationCol.NAME},
           app.state ${ApplicationCol.STATE}
      FROM $API_KEY api
     INNER JOIN $APPLICATION app ON app.application_id = api.application_id
     WHERE api.api_key_id = ?"""

private const val FIND_ALL_BY_APPLICATION_ID_SQL = """
    SELECT api.api_key_id ${ApiKeyCol.ID},
           api.name ${ApiKeyCol.NAME},
           api.secret ${ApiKeyCol.SECRET},
           api.created_at ${ApiKeyCol.CREATED_AT},
           api.expires_at ${ApiKeyCol.EXPIRES_AT},
           app.application_id ${ApplicationCol.ID},
           app.name ${ApplicationCol.NAME},
           app.state ${ApplicationCol.STATE}
      FROM $API_KEY api
     INNER JOIN $APPLICATION app ON app.application_id = api.application_id
     WHERE app.application_id = ?
     ORDER BY api.created_at DESC"""

private const val INSERT_SQL = """
    INSERT INTO $API_KEY(
        api_key_id,
        name,
        secret,
        application_id,
        created_at,
        expires_at)
    VALUES(?, ?, ?, ?, ?, ?)"""

private const val DELETE_SQL = "DELETE FROM $API_KEY WHERE api_key_id = ? AND application_id = ?"

class H2ApiKeyRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : ApiKeyRepository {

    private val singleMapper = SingleResultSetMapper { it.getApiKeyFromRow() }
    private val multipleMapper = MultipleResultSetMapper { it.getApiKeyFromRow() }

    override suspend fun findById(id: String) = dataSource.connection.use {
        queryRunner.query(
            it,
            FIND_BY_ID_SQL,
            singleMapper,
            id,
        )
    }

    override suspend fun findAllByApplicationId(applicationId: Long): List<ApiKey> = dataSource.connection.use {
        queryRunner.query(
            it,
            FIND_ALL_BY_APPLICATION_ID_SQL,
            multipleMapper,
            applicationId,
        )
    }

    override suspend fun create(apiKey: ApiKey) {
        dataSource.connection.use {
            queryRunner.update(
                it,
                INSERT_SQL,
                apiKey.id,
                apiKey.name,
                apiKey.secret,
                apiKey.application.id,
                apiKey.createdAt,
                apiKey.expiresAt,
            )
        }
    }

    override suspend fun delete(applicationId: Long, id: String) = dataSource.connection.use {
        val updatedRows = queryRunner.update(
            it,
            DELETE_SQL,
            id,
            applicationId,
        )

        updatedRows > 0
    }
}
