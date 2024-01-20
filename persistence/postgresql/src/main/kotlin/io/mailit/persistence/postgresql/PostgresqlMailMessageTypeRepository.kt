package io.mailit.persistence.postgresql

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.exception.PersistenceException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.postgresql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.postgresql.MailMessageContent.HTML
import io.mailit.persistence.postgresql.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.postgresql.Tables.MAIL_MESSAGE_TYPE
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.pgclient.PgException
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
     WHERE mt.mail_message_type_id = $1
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
     WHERE mt.name = $1
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
     LIMIT $1 OFFSET $2"""

private const val INSERT_PLAIN_TEXT_SQL = """
    INSERT INTO $MAIL_MESSAGE_TYPE(
        mail_message_type_id,
        name,
        description,
        max_retries_count,
        state,
        created_at,
        updated_at,
        content_type)
    VALUES($1, $2, $3, $4, $5, $6, $7, $8)
    """

private const val INSERT_HTML_SQL = """
    WITH new_mail_message_type AS (
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
        VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
        RETURNING mail_message_type_id
    )
    INSERT INTO $MAIL_MESSAGE_TEMPLATE(mail_message_type_id, template) 
    SELECT mail_message_type_id, $10 FROM new_mail_message_type
    """

private const val UPDATE_SQL = """
    WITH mail_message_template_update AS (
        UPDATE $MAIL_MESSAGE_TEMPLATE SET template = $5 WHERE mail_message_type_id = $6
    )
    UPDATE $MAIL_MESSAGE_TYPE SET
        description = $1,
        max_retries_count = $2,
        updated_at = $3,
        template_engine = $4
    WHERE mail_message_type_id = $6    
    """

private const val UPDATE_STATE_SQL = """
    UPDATE $MAIL_MESSAGE_TYPE SET
        state = $1,
        updated_at = $2
    WHERE mail_message_type_id = $3"""

class PostgresqlMailMessageTypeRepository(
    private val client: PgPool,
) : MailMessageTypeRepository {

    override suspend fun findById(id: Long): MailMessageType? =
        client.preparedQuery(FIND_BY_ID_SQL).execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageTypeFromRow() else null }
            .awaitSuspending()

    override suspend fun findByName(name: String): MailMessageType? =
        client.preparedQuery(FIND_BY_NAME_SQL).execute(Tuple.of(name))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageTypeFromRow() else null }
            .awaitSuspending()

    override suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val offset = page * size

        return client.preparedQuery(FIND_ALL_SLICED_SQL).execute(Tuple.of(size + 1, offset))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageTypeFromRow() }
            .collect().asList()
            .onItem().transform { createSlice(it, page, size) }
            .awaitSuspending()
    }

    override suspend fun create(mailMessageType: MailMessageType): MailMessageType {
        val prepared = when (mailMessageType) {
            is PlainTextMailMessageType -> prepareCreatePlainText(mailMessageType)
            is HtmlMailMessageType -> prepareCreateHtml(mailMessageType)
        }

        prepared
            .onFailure(PgException::class.java).transform {
                if ((it as? PgException)?.sqlState == "23505") DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()

        return mailMessageType
    }

    private fun prepareCreatePlainText(mailMessageType: PlainTextMailMessageType): Uni<RowSet<Row>> {
        val argumentsArray = arrayOf(
            mailMessageType.id,
            mailMessageType.name,
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.state.name,
            mailMessageType.createdAt.toLocalDateTime(),
            mailMessageType.updatedAt.toLocalDateTime(),
            mailMessageType.contentType,
        )

        return client.preparedQuery(INSERT_PLAIN_TEXT_SQL).execute(Tuple.from(argumentsArray))
    }

    private fun prepareCreateHtml(mailMessageType: HtmlMailMessageType): Uni<RowSet<Row>> {
        val argumentsArray = arrayOf(
            mailMessageType.id,
            mailMessageType.name,
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.state.name,
            mailMessageType.createdAt.toLocalDateTime(),
            mailMessageType.updatedAt.toLocalDateTime(),
            mailMessageType.contentType,
            mailMessageType.templateEngine.name,
            mailMessageType.template.compressedValue,
        )

        return client.preparedQuery(INSERT_HTML_SQL).execute(Tuple.from(argumentsArray))
    }

    override suspend fun update(mailMessageType: MailMessageType): MailMessageType {
        val arguments = arrayOf(
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.updatedAt.toLocalDateTime(),
            (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
            (mailMessageType as? HtmlMailMessageType)?.template?.compressedValue,
            mailMessageType.id,
        )

        val updatedRowsCount = client.preparedQuery(UPDATE_SQL).execute(Tuple.from(arguments))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

        if (updatedRowsCount == 0) {
            throw PersistenceException("MailMessageType with id ${mailMessageType.id} hasn't been updated")
        }

        return mailMessageType
    }

    override suspend fun updateState(id: Long, state: MailMessageTypeState, updatedAt: Instant): Int =
        client.preparedQuery(UPDATE_STATE_SQL).execute(Tuple.of(state.name, updatedAt.toLocalDateTime(), id))
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
