package it.mail.persistence.h2

import org.apache.commons.dbutils.ResultSetHandler
import java.sql.ResultSet

internal val IDS_MAPPER = MultipleIdsResultSetMapper()
internal val EXISTS_QUERY_MAPPER = ExistsQueryResultSetMapper()

/**
 * Used to extract list of ids. Thread safe
 */
internal class MultipleIdsResultSetMapper : ResultSetHandler<List<Long>> {

    override fun handle(rs: ResultSet?): List<Long> {
        if (rs == null) {
            return ArrayList()
        }

        val ids = ArrayList<Long>()
        while (rs.next()) {
            ids.add(rs.mapRowToLong())
        }
        return ids
    }

    private fun ResultSet.mapRowToLong(): Long = getLong(1)
}

/**
 * Used to verify if result exists. Thread safe
 */
internal class ExistsQueryResultSetMapper : ResultSetHandler<Boolean> {

    override fun handle(rs: ResultSet?): Boolean = rs?.next() ?: false
}
