package io.mailit.persistence.mysql

import com.mysql.cj.jdbc.MysqlDataSource
import io.agroal.api.security.SimplePassword
import io.agroal.pool.DataSource as AgroalDataSource
import io.mailit.persistence.common.createSchema
import io.mailit.persistence.common.dropSchema
import io.mailit.persistence.common.initializeSchema
import io.mailit.persistence.test.DatabaseSchemaTest
import io.mailit.test.readResource
import io.quarkus.test.junit.QuarkusTest
import java.sql.Connection

@QuarkusTest
class MysqlDatabaseSchemaTest : DatabaseSchemaTest() {

    private lateinit var referenceDatasource: MysqlDataSource

    override fun initialize() {
        dataSource.createSchema(SCHEMA_NAME)

        val properties = extractDatabaseProperties()

        referenceDatasource = MysqlDataSource().apply {
            serverName = "localhost"
            port = properties.port
            databaseName = SCHEMA_NAME
            user = properties.user
            password = properties.password
        }

        referenceDatasource.initializeSchema(SCHEMA_NAME, REFERENCE_SCRIPT_LOCATION.readResource())
    }

    private fun extractDatabaseProperties(): DatabaseProperties {
        val config = (dataSource as AgroalDataSource).configuration
            .connectionPoolConfiguration()
            .connectionFactoryConfiguration()

        val port = jdbcUrlRegex.matchEntire(config.jdbcUrl())!!.groups["PORT"]!!.value.toInt()
        val password = (config.credentials().first() as SimplePassword).word

        return DatabaseProperties(
            port = port,
            user = config.principal().name,
            password = password,
        )
    }

    override fun cleanUp() {
        dataSource.dropSchema(SCHEMA_NAME, cascade = false)
    }

    override fun useConnection(executable: (Connection) -> Unit) = referenceDatasource.connection.use(executable)

    companion object {
        private const val SCHEMA_NAME = "comparison_reference"
        private const val REFERENCE_SCRIPT_LOCATION = "db/expected_schema.sql"

        private val jdbcUrlRegex = Regex("^jdbc:mysql://localhost:(?<PORT>\\d+)/.+$")
    }
}

private data class DatabaseProperties(
    val port: Int,
    val user: String,
    val password: String,
)
