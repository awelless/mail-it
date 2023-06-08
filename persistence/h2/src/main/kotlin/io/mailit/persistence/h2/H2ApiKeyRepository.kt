package io.mailit.persistence.h2

import io.mailit.core.model.application.ApiKey
import io.mailit.core.spi.application.ApiKeyRepository
import java.sql.ResultSet
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler

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
     WHERE api.api_key_id = ?"""

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
    VALUES(?, ?, ?, ?, ?)"""

private const val DELETE_SQL = "DELETE FROM api_key WHERE api_key_id = ?"

class H2ApiKeyRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : ApiKeyRepository {

    override suspend fun findById(id: String) = dataSource.connection.use {
        queryRunner.query(
            it,
            FIND_BY_ID_SQL,
            SingleApiKeyResultSetMapper,
            id,
        )
    }

    override suspend fun findAll(applicationId: Long): List<ApiKey> = dataSource.connection.use {
        queryRunner.query(
            it,
            FIND_ALL_SQL,
            MultipleApiKeyResultSetMapper,
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
                apiKey.expiresAt,
            )
        }
    }

    override suspend fun delete(id: String) {
        dataSource.connection.use {
            queryRunner.update(
                it,
                DELETE_SQL,
                id,
            )
        }
    }
}

/**
 * Used to extract single [ApiKey]. Thread safe
 */
private object SingleApiKeyResultSetMapper : ResultSetHandler<ApiKey?> {

    override fun handle(rs: ResultSet?) =
        if (rs?.next() == true) {
            rs.getApiKeyFromRow()
        } else {
            null
        }
}

/**
 * Used to extract list of [ApiKey]s. Thread safe
 */
private object MultipleApiKeyResultSetMapper : ResultSetHandler<List<ApiKey>> {

    override fun handle(rs: ResultSet?): List<ApiKey> {
        if (rs == null) {
            return emptyList()
        }

        val apiKeys = mutableListOf<ApiKey>()
        while (rs.next()) {
            apiKeys += rs.getApiKeyFromRow()
        }
        return apiKeys
    }
}
