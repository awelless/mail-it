package io.mailit.persistence.postgresql

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.postgresql.Columns.MailMessage as MailMessageCol
import io.mailit.persistence.postgresql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.postgresql.Tables.MAIL_MESSAGE
import io.mailit.persistence.postgresql.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.postgresql.Tables.MAIL_MESSAGE_TYPE
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.WritePersistenceMail
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.pgclient.PgException
import java.time.Instant

private const val FIND_WITH_TYPE_BY_ID_SQL = """
    SELECT m.mail_message_id ${MailMessageCol.ID},
           m.text ${MailMessageCol.TEXT},
           m.data ${MailMessageCol.DATA},
           m.subject ${MailMessageCol.SUBJECT},
           m.email_from ${MailMessageCol.EMAIL_FROM},
           m.email_to ${MailMessageCol.EMAIL_TO},
           m.created_at ${MailMessageCol.CREATED_AT},
           m.sending_started_at ${MailMessageCol.SENDING_STARTED_AT},
           m.sent_at ${MailMessageCol.SENT_AT},
           m.state ${MailMessageCol.STATE},
           m.failed_count ${MailMessageCol.FAILED_COUNT},
           m.deduplication_id ${MailMessageCol.DEDUPLICATION_ID},
           mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
    FROM $MAIL_MESSAGE m
    INNER JOIN $MAIL_MESSAGE_TYPE mt ON m.mail_message_type_id = mt.mail_message_type_id
     LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id 
    WHERE m.mail_message_id = $1"""

private const val FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATEES_SQL = """
    SELECT m.mail_message_id ${MailMessageCol.ID},
           m.text ${MailMessageCol.TEXT},
           m.data ${MailMessageCol.DATA},
           m.subject ${MailMessageCol.SUBJECT},
           m.email_from ${MailMessageCol.EMAIL_FROM},
           m.email_to ${MailMessageCol.EMAIL_TO},
           m.created_at ${MailMessageCol.CREATED_AT},
           m.sending_started_at ${MailMessageCol.SENDING_STARTED_AT},
           m.sent_at ${MailMessageCol.SENT_AT},
           m.state ${MailMessageCol.STATE},
           m.failed_count ${MailMessageCol.FAILED_COUNT},
           m.deduplication_id ${MailMessageCol.DEDUPLICATION_ID},
           mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
    FROM $MAIL_MESSAGE m
    INNER JOIN $MAIL_MESSAGE_TYPE mt ON m.mail_message_type_id = mt.mail_message_type_id
    LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id
    WHERE m.sending_started_at < $1
      AND m.state = ANY($2)
    LIMIT $3"""

private const val FIND_IDS_BY_STATEES_SQL = """
    SELECT mail_message_id 
      FROM $MAIL_MESSAGE 
     WHERE state = ANY($1)
     LIMIT $2"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT m.mail_message_id ${MailMessageCol.ID},
           m.text ${MailMessageCol.TEXT},
           m.data ${MailMessageCol.DATA},
           m.subject ${MailMessageCol.SUBJECT},
           m.email_from ${MailMessageCol.EMAIL_FROM},
           m.email_to ${MailMessageCol.EMAIL_TO},
           m.created_at ${MailMessageCol.CREATED_AT},
           m.sending_started_at ${MailMessageCol.SENDING_STARTED_AT},
           m.sent_at ${MailMessageCol.SENT_AT},
           m.state ${MailMessageCol.STATE},
           m.failed_count ${MailMessageCol.FAILED_COUNT},
           m.deduplication_id ${MailMessageCol.DEDUPLICATION_ID},
           mt.mail_message_type_id ${MailMessageTypeCol.ID},
           mt.name ${MailMessageTypeCol.NAME},
           mt.description ${MailMessageTypeCol.DESCRIPTION},
           mt.max_retries_count ${MailMessageTypeCol.MAX_RETRIES_COUNT},
           mt.state ${MailMessageTypeCol.STATE},
           mt.created_at ${MailMessageTypeCol.CREATED_AT},
           mt.updated_at ${MailMessageTypeCol.UPDATED_AT},
           mt.content_type ${MailMessageTypeCol.CONTENT_TYPE},
           mt.template_engine ${MailMessageTypeCol.TEMPLATE_ENGINE},
           t.template ${MailMessageTypeCol.TEMPLATE}
     FROM $MAIL_MESSAGE m
    INNER JOIN $MAIL_MESSAGE_TYPE mt ON m.mail_message_type_id = mt.mail_message_type_id
     LEFT JOIN $MAIL_MESSAGE_TEMPLATE t ON mt.mail_message_type_id = t.mail_message_type_id
    ORDER BY m.mail_message_id DESC
    LIMIT $1 OFFSET $2"""

private const val INSERT_SQL = """
   INSERT INTO $MAIL_MESSAGE(
        mail_message_id,
        text,
        data,
        subject,
        email_from,
        email_to,
        mail_message_type_id,
        created_at,
        sending_started_at,
        sent_at,
        state,
        failed_count,
        deduplication_id)
   VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)"""

private const val UPDATE_STATE_SQL = "UPDATE $MAIL_MESSAGE SET state = $1 WHERE mail_message_id = $2"

private const val UPDATE_STATE_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE SET 
        state = $1, 
        sending_started_at = $2 
    WHERE mail_message_id = $3 
      AND state = ANY($4)"""

private const val UPDATE_STATE_AND_SENT_AT_SQL = "UPDATE $MAIL_MESSAGE SET state = $1, sent_at = $2 WHERE mail_message_id = $3"

private const val UPDATE_STATE_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE mail_message SET 
        state = $1, 
        failed_count = $2, 
        sending_started_at = $3 
    WHERE mail_message_id = $4"""

class PostgresqlMailMessageRepository(
    private val client: PgPool,
    private val dataSerializer: MailMessageDataSerializer,
) : MailMessageRepository, MailRepository {

    override suspend fun findOneWithTypeById(id: MailId): MailMessage? =
        client.preparedQuery(FIND_WITH_TYPE_BY_ID_SQL).execute(Tuple.of(id.value))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageWithTypeFromRow(dataSerializer) else null }
            .awaitSuspending()

    override suspend fun findAllWithTypeByStatesAndSendingStartedBefore(
        states: Collection<MailState>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage> {
        val stateNames = states
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATEES_SQL)
            .execute(Tuple.of(sendingStartedBefore.toLocalDateTime(), stateNames, maxListSize))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllIdsByStateIn(states: Collection<MailState>, maxListSize: Int): List<MailId> {
        val stateNames = states
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(FIND_IDS_BY_STATEES_SQL).execute(Tuple.of(stateNames, maxListSize))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { MailId(it.getLong("mail_message_id")) }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage> {
        val offset = page * size

        return client.preparedQuery(FIND_ALL_SLICED_SQL).execute(Tuple.of(size + 1, offset))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .onItem().transform { createSlice(it, page, size) }
            .awaitSuspending()
    }

    override suspend fun create(mail: WritePersistenceMail): Result<Unit> {
        val data = dataSerializer.write(mail.data)

        val argumentsArray = arrayOf(
            mail.id.value,
            mail.text,
            data,
            mail.subject,
            mail.emailFrom?.email,
            mail.emailTo.email,
            mail.mailTypeId.value,
            mail.createdAt.toLocalDateTime(),
            mail.sendingStartedAt?.toLocalDateTime(),
            mail.sentAt?.toLocalDateTime(),
            mail.state.name,
            mail.failedCount,
            mail.deduplicationId,
        )

        return client.preparedQuery(INSERT_SQL)
            .execute(Tuple.from(argumentsArray))
            .onItem().transform { Result.success(Unit) }
            .onFailure().recoverWithItem { e: Throwable ->
                val err = if ((e as? PgException)?.sqlState == "23505") DuplicateUniqueKeyException(e.message, e) else e
                Result.failure(err)
            }
            .awaitSuspending()
    }

    override suspend fun updateMessageState(id: MailId, state: MailState): Int =
        client.preparedQuery(UPDATE_STATE_SQL).execute(Tuple.of(state.name, id.value))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

    override suspend fun updateMessageStateAndSendingStartedTimeByIdAndStateIn(
        id: MailId,
        states: Collection<MailState>,
        state: MailState,
        sendingStartedAt: Instant,
    ): Int {
        val stateNames = states
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(UPDATE_STATE_AND_SENDING_START_SQL)
            .execute(Tuple.of(state.name, sendingStartedAt.toLocalDateTime(), id.value, stateNames))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
    }

    override suspend fun updateMessageStateAndSentTime(id: MailId, state: MailState, sentAt: Instant): Int =
        client.preparedQuery(UPDATE_STATE_AND_SENT_AT_SQL)
            .execute(Tuple.of(state.name, sentAt.toLocalDateTime(), id.value))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

    override suspend fun updateMessageStateFailedCountAndSendingStartedTime(
        id: MailId,
        state: MailState,
        failedCount: Int,
        sendingStartedAt: Instant?,
    ): Int =
        client.preparedQuery(UPDATE_STATE_FAILED_COUNT_AND_SENDING_START_SQL)
            .execute(Tuple.of(state.name, failedCount, sendingStartedAt?.toLocalDateTime(), id.value))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
}
