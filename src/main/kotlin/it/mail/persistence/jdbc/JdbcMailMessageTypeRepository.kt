package it.mail.persistence.jdbc

import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState
import it.mail.domain.Slice
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.persistence.common.IdGenerator
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import java.sql.ResultSet
import javax.sql.DataSource

private const val FIND_BY_ID_SQL = """
    SELECT mail_message_type_id mt_mail_message_type_id,
           name mt_name,
           description mt_description,
           max_retries_count mt_max_retries_count,
           state mt_state
      FROM mail_message_type
     WHERE mail_message_type_id = ?
"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT mail_message_type_id mt_mail_message_type_id,
           name mt_name,
           description mt_description,
           max_retries_count mt_max_retries_count,
           state mt_state
      FROM mail_message_type
     LIMIT ? OFFSET ?
"""

private const val EXISTS_BY_NAME_SQL = "SELECT 1 FROM mail_message_type WHERE name = ?"
private const val INSERT_SQL = "INSERT INTO mail_message_type(mail_message_type_id, name, description, max_retries_count, state) VALUES(?, ?, ?, ?, ?)"
private const val UPDATE_DESCRIPTION_MAX_RETRIES_COUNT_SQL = "UPDATE mail_message_type SET description = ?, max_retries_count = ? WHERE mail_message_type_id = ?"
private const val UPDATE_STATE_SQL = "UPDATE mail_message_type SET state = ? WHERE mail_message_type_id = ?"

class JdbcMailMessageTypeRepository(
    private val idGenerator: IdGenerator,
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : MailMessageTypeRepository {

    private val singleItemMapper = SingleMailMessageTypeResultSetMapper()
    private val multipleItemsMapper = MultipleMailMessageTypesResultSetMapper()

    override suspend fun findById(id: Long): MailMessageType? =
        dataSource.connection.use {
            queryRunner.query(
                it, FIND_BY_ID_SQL,
                singleItemMapper,
                id
            )
        }

    override suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val offset = page * size

        val content = dataSource.connection.use {
            queryRunner.query(
                it, FIND_ALL_SLICED_SQL,
                multipleItemsMapper,
                size, offset
            )
        }

        return Slice(content, page, size)
    }

    override suspend fun existsOneWithName(name: String): Boolean =
        dataSource.connection.use {
            queryRunner.query(
                it, EXISTS_BY_NAME_SQL,
                EXISTS_QUERY_MAPPER,
                name
            )
        }

    override suspend fun create(mailMessageType: MailMessageType): MailMessageType {
        val id = idGenerator.generateId()

        dataSource.connection.use {
            queryRunner.update(
                it, INSERT_SQL,
                id, mailMessageType.name, mailMessageType.description, mailMessageType.maxRetriesCount, mailMessageType.state.name
            )
        }

        mailMessageType.id = id
        return mailMessageType
    }

    override suspend fun updateDescriptionAndMaxRetriesCount(id: Long, description: String?, maxRetriesCount: Int?): Int =
        dataSource.connection.use {
            queryRunner.update(
                it, UPDATE_DESCRIPTION_MAX_RETRIES_COUNT_SQL,
                description, maxRetriesCount, id
            )
        }

    override suspend fun updateState(id: Long, state: MailMessageTypeState): Int =
        dataSource.connection.use {
            queryRunner.update(
                it, UPDATE_STATE_SQL,
                state, id
            )
        }
}

/**
 * Used to extract single mail type. Thread safe
 */
private class SingleMailMessageTypeResultSetMapper : ResultSetHandler<MailMessageType?> {

    override fun handle(rs: ResultSet?): MailMessageType? =
        if (rs?.next() == true) {
            rs.getMailMessageTypeFromRow()
        } else {
            null
        }
}

/**
 * Used to extract list of mail type. Thread safe
 */
private class MultipleMailMessageTypesResultSetMapper : ResultSetHandler<List<MailMessageType>> {

    override fun handle(rs: ResultSet?): List<MailMessageType> {
        if (rs == null) {
            return ArrayList()
        }

        val mailTypes = ArrayList<MailMessageType>()
        while (rs.next()) {
            mailTypes.add(rs.getMailMessageTypeFromRow())
        }
        return mailTypes
    }
}
