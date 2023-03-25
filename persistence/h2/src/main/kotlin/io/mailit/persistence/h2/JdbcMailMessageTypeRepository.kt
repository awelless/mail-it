package io.mailit.persistence.h2

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.model.Slice
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.core.spi.PersistenceException
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.h2.MailMessageContent.HTML
import io.mailit.persistence.h2.MailMessageContent.PLAIN_TEXT
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import org.h2.api.ErrorCode

private const val FIND_BY_ID_SQL = """
    SELECT mail_message_type_id mt_mail_message_type_id,
           name mt_name,
           description mt_description,
           max_retries_count mt_max_retries_count,
           state mt_state,
           created_at mt_created_at,
           updated_at mt_updated_at,
           content_type mt_content_type,
           template_engine mt_template_engine,
           template mt_template
      FROM mail_message_type
     WHERE mail_message_type_id = ?
       AND state = 'ENABLED'"""

private const val FIND_BY_NAME_SQL = """
    SELECT mail_message_type_id mt_mail_message_type_id,
           name mt_name,
           description mt_description,
           max_retries_count mt_max_retries_count,
           state mt_state,
           created_at mt_created_at,
           updated_at mt_updated_at,
           content_type mt_content_type,
           template_engine mt_template_engine,
           template mt_template
      FROM mail_message_type
     WHERE name = ?
       AND state = 'ENABLED'"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT mail_message_type_id mt_mail_message_type_id,
           name mt_name,
           description mt_description,
           max_retries_count mt_max_retries_count,
           state mt_state,
           created_at mt_created_at,
           updated_at mt_updated_at,
           content_type mt_content_type,
           template_engine mt_template_engine,
           template mt_template
      FROM mail_message_type
     WHERE state = 'ENABLED'
     ORDER BY mt_mail_message_type_id DESC
     LIMIT ? OFFSET ?"""

private const val INSERT_SQL = """
    INSERT INTO mail_message_type(
        mail_message_type_id,
        name,
        description,
        max_retries_count,
        state,
        created_at,
        updated_at,
        content_type,
        template_engine,
        template)
    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

private const val UPDATE_SQL = """
    UPDATE mail_message_type SET
        description = ?,
        max_retries_count = ?,
        updated_at = ?,
        template_engine = ?,
        template = ?
    WHERE mail_message_type_id = ?"""

private const val UPDATE_STATE_SQL = """
    UPDATE mail_message_type SET
        state = ?,
        updated_at = ?
    WHERE mail_message_type_id = ?"""

class JdbcMailMessageTypeRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : MailMessageTypeRepository {

    private val singleItemMapper = SingleMailMessageTypeResultSetMapper()
    private val multipleItemsMapper = MultipleMailMessageTypesResultSetMapper()

    override suspend fun findById(id: Long): MailMessageType? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_BY_ID_SQL,
                singleItemMapper,
                id,
            )
        }

    override suspend fun findByName(name: String): MailMessageType? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_BY_NAME_SQL,
                singleItemMapper,
                name,
            )
        }

    override suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val offset = page * size

        val content = dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_ALL_SLICED_SQL,
                multipleItemsMapper,
                size + 1,
                offset,
            )
        }

        return createSlice(content, page, size)
    }

    override suspend fun create(mailMessageType: MailMessageType): MailMessageType {
        try {
            dataSource.connection.use {
                queryRunner.update(
                    it, INSERT_SQL,
                    mailMessageType.id,
                    mailMessageType.name,
                    mailMessageType.description,
                    mailMessageType.maxRetriesCount,
                    mailMessageType.state.name,
                    mailMessageType.createdAt,
                    mailMessageType.updatedAt,
                    mailMessageType.contentType,
                    (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
                    (mailMessageType as? HtmlMailMessageType)?.template,
                )
            }
        } catch (e: SQLException) {
            // replace with custom exception handler?
            throw if (e.errorCode == ErrorCode.DUPLICATE_KEY_1) {
                DuplicateUniqueKeyException(e.message, e)
            } else {
                e
            }
        }

        return mailMessageType
    }

    override suspend fun update(mailMessageType: MailMessageType): MailMessageType {
        val updatedRowsCount = dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_SQL,
                mailMessageType.description,
                mailMessageType.maxRetriesCount,
                mailMessageType.updatedAt,
                (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
                (mailMessageType as? HtmlMailMessageType)?.template,
                mailMessageType.id,
            )
        }

        if (updatedRowsCount == 0) {
            throw PersistenceException("MailMessageType with id ${mailMessageType.id} hasn't been updated")
        }

        return mailMessageType
    }

    override suspend fun updateState(id: Long, state: MailMessageTypeState, updatedAt: Instant): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATE_SQL,
                state.name,
                updatedAt,
                id,
            )
        }

    private val MailMessageType.contentType: String
        get() = when (this) {
            is PlainTextMailMessageType -> PLAIN_TEXT.name
            is HtmlMailMessageType -> HTML.name
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

internal enum class MailMessageContent {

    PLAIN_TEXT,
    HTML,
}
