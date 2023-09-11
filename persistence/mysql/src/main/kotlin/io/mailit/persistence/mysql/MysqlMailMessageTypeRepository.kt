package io.mailit.persistence.mysql

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.model.Slice
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.core.spi.PersistenceException
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.mysql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.mysql.MailMessageContent.HTML
import io.mailit.persistence.mysql.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.mysql.Tables.MAIL_MESSAGE_TYPE
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.mysqlclient.MySQLPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.mysqlclient.MySQLException
import java.time.Instant

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
       AND state = 'ENABLED'"""

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

private const val UPDATE_TYPE_SQL = """
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

class MysqlMailMessageTypeRepository(
    private val client: MySQLPool,
) : MailMessageTypeRepository {

    override suspend fun findById(id: Long): MailMessageType? =
        client.preparedQuery(FIND_BY_ID_SQL)
            .execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageTypeFromRow() else null }
            .awaitSuspending()

    override suspend fun findByName(name: String): MailMessageType? =
        client.preparedQuery(FIND_BY_NAME_SQL)
            .execute(Tuple.of(name))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageTypeFromRow() else null }
            .awaitSuspending()

    override suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val offset = page * size

        return client.preparedQuery(FIND_ALL_SLICED_SQL)
            .execute(Tuple.of(size + 1, offset))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageTypeFromRow() }
            .collect().asList()
            .onItem().transform { createSlice(it, page, size) }
            .awaitSuspending()
    }

    override suspend fun create(mailMessageType: MailMessageType): MailMessageType {
        val arguments = arrayOf(
            mailMessageType.id,
            mailMessageType.name,
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.state.name,
            mailMessageType.createdAt.toLocalDateTime(),
            mailMessageType.updatedAt.toLocalDateTime(),
            mailMessageType.contentType,
            (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
        )

        client.withTransaction { connection ->
            val query = connection.preparedQuery(INSERT_MAIL_TYPE_SQL)
                .execute(Tuple.from(arguments))
                .onFailure(MySQLException::class.java).transform {
                    if ((it as? MySQLException)?.errorCode == 23505) DuplicateUniqueKeyException(it.message, it) else it
                }

            if (mailMessageType is HtmlMailMessageType) {
                query.onItem().transformToUni { _ ->
                    connection.preparedQuery(INSERT_MAIL_TEMPLATE_SQL)
                        .execute(Tuple.of(mailMessageType.id, mailMessageType.template.compressedValue.toBuffer()))
                }
            } else {
                query
            }
        }.awaitSuspending()

        return mailMessageType
    }

    override suspend fun update(mailMessageType: MailMessageType): MailMessageType {
        val arguments = arrayOf(
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.updatedAt.toLocalDateTime(),
            (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
            mailMessageType.id,
        )

        client.withTransaction { connection ->
            val query = connection.preparedQuery(UPDATE_TYPE_SQL)
                .execute(Tuple.from(arguments))
                .onItem().transform {
                    if (it.rowCount() == 0) throw PersistenceException("MailMessageType with id ${mailMessageType.id} hasn't been updated") else it
                }

            if (mailMessageType is HtmlMailMessageType) {
                query.onItem().transformToUni { _ ->
                    connection.preparedQuery(UPDATE_STATE_SQL)
                        .execute(Tuple.of(mailMessageType.template.compressedValue.toBuffer(), mailMessageType.id))
                }
            } else {
                query
            }
        }.awaitSuspending()

        return mailMessageType
    }

    override suspend fun updateState(id: Long, state: MailMessageTypeState, updatedAt: Instant): Int =
        client.preparedQuery(UPDATE_STATE_SQL)
            .execute(Tuple.of(state.name, updatedAt.toLocalDateTime(), id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

    private val MailMessageType.contentType: String
        get() = when (this) {
            is PlainTextMailMessageType -> MailMessageContent.PLAIN_TEXT.name
            is HtmlMailMessageType -> HTML.name
        }
}

internal enum class MailMessageContent {
    PLAIN_TEXT,
    HTML,
}
