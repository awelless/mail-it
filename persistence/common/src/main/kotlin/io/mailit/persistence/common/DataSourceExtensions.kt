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
            statement.execute("CREATE SCHEMA $schemaName")
        }
    }
}

fun DataSource.initializeSchema(schemaName: String, sqlScript: String) = useConnectionWithSchema(schemaName) { connection ->
    val initializationStatements = sqlScript.split(';')
        .filter { it.isNotBlank() }

    initializationStatements.forEach { sql ->
        connection.createStatement().use { statement ->
            statement.execute(sql)
        }
    }
}

fun DataSource.dropSchema(schemaName: String, cascade: Boolean = true) {
    connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.execute("DROP SCHEMA $schemaName ${if (cascade) "CASCADE" else ""}")
        }
    }
}
