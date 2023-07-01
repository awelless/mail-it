package io.mailit.persistence.h2

import io.mailit.core.model.application.ApiKey
import java.sql.ResultSet
import org.apache.commons.dbutils.ResultSetHandler

/**
 * Used to extract single object. Thread safe
 */
internal class SingleResultSetMapper<T>(private val mapper: (ResultSet) -> T) : ResultSetHandler<T?> {
    override fun handle(rs: ResultSet?) = if (rs?.next() == true) mapper(rs) else null
}

/**
 * Used to extract list of [ApiKey]s. Thread safe
 */
internal class MultipleResultSetMapper<T>(private val mapper: (ResultSet) -> T) : ResultSetHandler<List<T>> {

    override fun handle(rs: ResultSet?): List<T> {
        if (rs == null) {
            return emptyList()
        }

        val objects = mutableListOf<T>()
        while (rs.next()) {
            objects += mapper(rs)
        }
        return objects
    }
}

/**
 * Used to extract list of ids. Thread safe
 */
internal object MultipleIdsResultSetMapper : ResultSetHandler<List<Long>> {

    override fun handle(rs: ResultSet?): List<Long> {
        if (rs == null) {
            return emptyList()
        }

        val ids = mutableListOf<Long>()
        while (rs.next()) {
            ids.add(rs.mapRowToLong())
        }
        return ids
    }

    private fun ResultSet.mapRowToLong(): Long = getLong(1)
}
