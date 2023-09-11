package io.mailit.persistence.test

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

abstract class DatabaseSchemaTest {

    @Inject
    lateinit var dataSource: DataSource

    @BeforeEach
    fun setUp() = initialize()

    @AfterEach
    fun tearDown() = cleanUp()

    abstract fun initialize()

    abstract fun cleanUp()

    abstract fun useConnection(executable: (Connection) -> Unit)

    @Test
    fun `compare schemas`() {
        dataSource.connection.use { connection ->
            useConnection { referenceConnection ->
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
