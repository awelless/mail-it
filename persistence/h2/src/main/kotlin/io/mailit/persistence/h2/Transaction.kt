package io.mailit.persistence.h2

import java.sql.Connection
import java.sql.SQLException

internal inline fun <T> Connection.withTransaction(executable: () -> T) = try {
    autoCommit = false
    executable().also { commit() }
} catch (e: SQLException) {
    rollback()
    throw e
} finally {
    autoCommit = true
}
