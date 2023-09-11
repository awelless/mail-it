package io.mailit.persistence.postgresql

import io.mailit.persistence.common.createSchema
import io.mailit.persistence.common.dropSchema
import io.mailit.persistence.common.initializeSchema
import io.mailit.persistence.common.useConnectionWithSchema
import io.mailit.persistence.test.DatabaseSchemaTest
import io.mailit.test.readResource
import io.quarkus.test.junit.QuarkusTest
import java.sql.Connection

@QuarkusTest
class PostgresqlDatabaseSchemaTest : DatabaseSchemaTest() {

    override fun initialize() {
        dataSource.createSchema(SCHEMA_NAME)
        dataSource.initializeSchema(SCHEMA_NAME, REFERENCE_SCRIPT_LOCATION.readResource())
    }

    override fun cleanUp() {
        dataSource.dropSchema(SCHEMA_NAME)
    }

    override fun useConnection(executable: (Connection) -> Unit) = dataSource.useConnectionWithSchema(SCHEMA_NAME, executable)

    companion object {
        private const val SCHEMA_NAME = "comparison_reference"
        private const val REFERENCE_SCRIPT_LOCATION = "db/expected_schema.sql"
    }
}
