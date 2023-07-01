package io.mailit.persistence.postgresql

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
import io.mailit.persistence.postgresql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.postgresql.MailMessageContent.HTML
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.pgclient.PgException
import java.time.Instant

// todo unify queries

private const val FIND_BY_ID_SQL = """
    SELECT mail_message_type_id ${MailMessageTypeCol.ID},
           name ${MailMessageTypeCol.NAME},
           description ${MailMessageTypeCol.DESCRIPTION},
           max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           state ${MailMessageTypeCol.STATE},
           created_at ${MailMessageTypeCol.CREATED_AT},
           updated_at ${MailMessageTypeCol.UPDATED_AT},
           content_type ${MailMessageTypeCol.CONTENT_TYPE},
           template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           template ${MailMessageTypeCol.TEMPLATE}
      FROM mail_message_type
     WHERE mail_message_type_id = $1
       AND state = 'ENABLED'"""

private const val FIND_BY_NAME_SQL = """
    SELECT mail_message_type_id ${MailMessageTypeCol.ID},
           name ${MailMessageTypeCol.NAME},
           description ${MailMessageTypeCol.DESCRIPTION},
           max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           state ${MailMessageTypeCol.STATE},
           created_at ${MailMessageTypeCol.CREATED_AT},
           updated_at ${MailMessageTypeCol.UPDATED_AT},
           content_type ${MailMessageTypeCol.CONTENT_TYPE},
           template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           template ${MailMessageTypeCol.TEMPLATE}
      FROM mail_message_type
     WHERE name = $1
       AND state = 'ENABLED'"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT mail_message_type_id ${MailMessageTypeCol.ID},
           name ${MailMessageTypeCol.NAME},
           description ${MailMessageTypeCol.DESCRIPTION},
           max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           state ${MailMessageTypeCol.STATE},
           created_at ${MailMessageTypeCol.CREATED_AT},
           updated_at ${MailMessageTypeCol.UPDATED_AT},
           content_type ${MailMessageTypeCol.CONTENT_TYPE},
           template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           template ${MailMessageTypeCol.TEMPLATE}
      FROM mail_message_type
     WHERE state = 'ENABLED'
     ORDER BY mt_mail_message_type_id DESC
     LIMIT $1 OFFSET $2"""

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
    VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)"""

private const val UPDATE_SQL = """
    UPDATE mail_message_type SET
        description = $1,
        max_retries_count = $2,
        updated_at = $3,
        template_engine = $4,
        template = $5
    WHERE mail_message_type_id = $6"""

private const val UPDATE_STATE_SQL = """
    UPDATE mail_message_type SET
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
        val argumentsArray = arrayOf(
            mailMessageType.id,
            mailMessageType.name,
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.state.name,
            mailMessageType.createdAt.toLocalDateTime(),
            mailMessageType.updatedAt.toLocalDateTime(),
            mailMessageType.contentType,
            (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
            (mailMessageType as? HtmlMailMessageType)?.template,
        )

        client.preparedQuery(INSERT_SQL).execute(Tuple.from(argumentsArray))
            .onFailure(PgException::class.java).transform {
                if ((it as? PgException)?.sqlState == "23505") DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()

        return mailMessageType
    }

    override suspend fun update(mailMessageType: MailMessageType): MailMessageType {
        val arguments = arrayOf(
            mailMessageType.description,
            mailMessageType.maxRetriesCount,
            mailMessageType.updatedAt.toLocalDateTime(),
            (mailMessageType as? HtmlMailMessageType)?.templateEngine?.name,
            (mailMessageType as? HtmlMailMessageType)?.template,
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
