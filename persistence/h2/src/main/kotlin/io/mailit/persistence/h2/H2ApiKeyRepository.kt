package io.mailit.persistence.h2

import io.mailit.apikey.spi.persistence.ApiKey
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.persistence.h2.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.h2.Tables.API_KEY
import java.sql.SQLException
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.h2.api.ErrorCode

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

    override suspend fun findAll(): List<ApiKey> = dataSource.connection.use {
        queryRunner.query(it, FIND_ALL_BY_APPLICATION_ID_SQL, multipleMapper)
    }

    override suspend fun create(apiKey: ApiKey): Result<Unit> {
        try {
            dataSource.connection.use {
                queryRunner.update(
                    it,
                    INSERT_SQL,
                    apiKey.id,
                    apiKey.name,
                    apiKey.secret,
                    apiKey.createdAt,
                    apiKey.expiresAt,
                )
            }
        } catch (e: SQLException) {
            // replace with custom exception handler?
            if (e.errorCode == ErrorCode.DUPLICATE_KEY_1) {
                return Result.failure(DuplicateUniqueKeyException(e.message, e))
            } else {
                throw e
            }
        }

        return Result.success(Unit)
    }

    override suspend fun delete(id: String) = dataSource.connection.use {
        val updatedRows = queryRunner.update(it, DELETE_SQL, id)
        updatedRows > 0
    }
}
