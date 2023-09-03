package io.mailit.persistence.common

import java.sql.Connection
import javax.sql.DataSource

fun DataSource.useConnectionWithSchema(schemaName: String, executable: (Connection) -> Unit) = connection.use {
    val previousSchema = it.schema

    try {
        it.schema = schemaName
        executable(it)
    } finally {
        it.schema = previousSchema
    }
}

fun DataSource.createSchema(schemaName: String) {
    connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("CREATE SCHEMA $schemaName")
        }
    }
}

fun DataSource.initializeSchema(schemaName: String, sqlScript: String) = useConnectionWithSchema(schemaName) { connection ->
    val initializationStatements = sqlScript.split(';')

    connection.createStatement().use { statement ->
        initializationStatements.forEach { statement.addBatch(it) }
        statement.executeBatch()
    }
}

fun DataSource.dropSchema(schemaName: String) {
    connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("DROP SCHEMA $schemaName CASCADE")
        }
    }
}
