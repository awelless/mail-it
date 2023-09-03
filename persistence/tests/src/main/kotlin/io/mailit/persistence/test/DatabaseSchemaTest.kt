package io.mailit.persistence.test

import io.mailit.persistence.common.createSchema
import io.mailit.persistence.common.dropSchema
import io.mailit.persistence.common.initializeSchema
import io.mailit.persistence.common.useConnectionWithSchema
import io.mailit.test.readResource
import jakarta.inject.Inject
import java.sql.Connection
import javax.sql.DataSource
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.DiffGeneratorFactory
import liquibase.diff.DiffResult
import liquibase.diff.compare.CompareControl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class DatabaseSchemaTest(
    private val referenceSchemaName: String,
    private val referenceSchemaScriptLocation: String,
) {

    @Inject
    lateinit var dataSource: DataSource

    @BeforeEach
    fun setUp() {
        dataSource.createSchema(referenceSchemaName)
        dataSource.initializeSchema(referenceSchemaName, referenceSchemaScriptLocation.readResource())
    }

    @AfterEach
    fun tearDown() {
        dataSource.dropSchema(referenceSchemaName)
    }

    @Test
    fun `compare schemas`() {
        dataSource.connection.use { connection ->
            dataSource.useConnectionWithSchema(referenceSchemaName) { referenceConnection ->
                val database = connection.toDatabase()
                val referenceDatabase = referenceConnection.toDatabase()

                val diff = DiffGeneratorFactory.getInstance().compare(referenceDatabase, database, CompareControl.STANDARD)

                assertTrue(diff.areEqual(), diff.format())
            }
        }
    }

    private fun Connection.toDatabase() = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(this))

    private fun DiffResult.format() = """
        Missing objects: $missingObjects
        Unexpected objects: $unexpectedObjects
        Changed objects: $changedObjects
    """.trimIndent()
}
