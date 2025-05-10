package io.mailit.persistence.h2

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.h2.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.h2.MailMessageContent.HTML
import io.mailit.persistence.h2.MailMessageContent.PLAIN_TEXT
import io.mailit.persistence.h2.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.h2.Tables.MAIL_MESSAGE_TYPE
import io.mailit.value.MailTypeId
import io.mailit.value.Slice
import io.mailit.value.exception.DuplicateUniqueKeyException
import io.mailit.value.exception.PersistenceException
import java.sql.SQLException
import java.time.Instant
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.h2.api.ErrorCode

private const val FIND_BY_ID_SQL = """
    SELECT mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
      FROM $MAIL_MESSAGE_TYPE mt
      LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id
     WHERE mt.mail_message_type_id = ?
       AND mt.state = 'ENABLED'"""

private const val FIND_BY_NAME_SQL = """
    SELECT mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
      FROM $MAIL_MESSAGE_TYPE mt
      LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id
     WHERE mt.name = ?
       AND mt.state = 'ENABLED'"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
      FROM $MAIL_MESSAGE_TYPE mt
      LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id
     WHERE mt.state = 'ENABLED'
     ORDER BY mt.mail_message_type_id DESC
     LIMIT ? OFFSET ?"""

private const val INSERT_MAIL_TYPE_SQL = """
    INSERT INTO $MAIL_MESSAGE_TYPE(
        mail_message_type_id,
        name,
        description,
        max_retries_count,
        state,
        created_at,
        updated_at,
        content_type,
        template_engine)
    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)"""

private const val INSERT_MAIL_TEMPLATE_SQL = """
    INSERT INTO $MAIL_MESSAGE_TEMPLATE(mail_message_type_id, template)
    VALUES(?, ?)
"""

private const val UPDATE_MAIL_TYPE_SQL = """
    UPDATE $MAIL_MESSAGE_TYPE SET
        description = ?,
        max_retries_count = ?,
        updated_at = ?,
        template_engine = ?
    WHERE mail_message_type_id = ?"""

private const val UPDATE_TEMPLATE_SQL = """
    UPDATE $MAIL_MESSAGE_TEMPLATE SET
        template = ?
    WHERE mail_message_type_id = ?
"""

private const val UPDATE_STATE_SQL = """
    UPDATE $MAIL_MESSAGE_TYPE SET
        state = ?,
        updated_at = ?
    WHERE mail_message_type_id = ?"""

class H2MailMessageTypeRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) : MailMessageTypeRepository {

    private val singleMapper = SingleResultSetMapper { it.getMailMessageTypeFromRow() }
    private val multipleMapper = MultipleResultSetMapper { it.getMailMessageTypeFromRow() }

    override suspend fun findById(id: MailTypeId): MailMessageType? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_BY_ID_SQL,
                singleMapper,
                id.value,
            )
        }

    override suspend fun findByName(name: String): MailMessageType? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_BY_NAME_SQL,
                singleMapper,
                name,
            )
        }

    override suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val offset = page * size

        val content = dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_ALL_SLICED_SQL,
                multipleMapper,
                size + 1,
                offset,
            )
        }

        return createSlice(content, page, size)
    }

    override suspend fun create(mailMessageType: MailMessageType): MailMessageType {
        try {
            dataSource.connection.use {
                it.withTransaction {
                    queryRunner.update(
                        it, INSERT_MAIL_TYPE_SQL,
                        mailMessageType.id.value,
                        mailMessageType.name,
                        mailMessageType.description,
                        mailMessageType.maxRetriesCount,
                        mailMessageType.state.name,
                        mailMessageType.createdAt,
                        mailMessageType.updatedAt,
                        mailMessageType.contentType,
                        (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
                    )

                    if (mailMessageType is HtmlMailMessageType) {
                        queryRunner.update(it, INSERT_MAIL_TEMPLATE_SQL, mailMessageType.id.value, mailMessageType.template.compressedValue)
                    }
                }
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
            it.withTransaction {
                queryRunner.update(
                    it,
                    UPDATE_TEMPLATE_SQL,
                    (mailMessageType as? HtmlMailMessageType)?.template?.compressedValue,
                    mailMessageType.id.value,
                )

                queryRunner.update(
                    it,
                    UPDATE_MAIL_TYPE_SQL,
                    mailMessageType.description,
                    mailMessageType.maxRetriesCount,
                    mailMessageType.updatedAt,
                    (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
                    mailMessageType.id.value,
                )
            }
        }

        if (updatedRowsCount == 0) {
            throw PersistenceException("MailMessageType with id ${mailMessageType.id} hasn't been updated")
        }

        return mailMessageType
    }

    override suspend fun updateState(id: MailTypeId, state: MailMessageTypeState, updatedAt: Instant): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATE_SQL,
                state.name,
                updatedAt,
                id.value,
            )
        }

    private val MailMessageType.contentType: String
        get() = when (this) {
            is PlainTextMailMessageType -> PLAIN_TEXT.name
            is HtmlMailMessageType -> HTML.name
        }
}

internal enum class MailMessageContent {
    PLAIN_TEXT,
    HTML,
}
