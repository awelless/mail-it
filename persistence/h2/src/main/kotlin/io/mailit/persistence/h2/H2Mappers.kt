package io.mailit.persistence.h2

import java.sql.ResultSet
import org.apache.commons.dbutils.ResultSetHandler

/**
 * Used to extract single object. Thread safe
 */
internal class SingleResultSetMapper<T>(private val mapper: (ResultSet) -> T) : ResultSetHandler<T?> {
    override fun handle(rs: ResultSet?) = if (rs?.next() == true) mapper(rs) else null
}

/**
 * Used to extract list of objects. Thread safe
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
