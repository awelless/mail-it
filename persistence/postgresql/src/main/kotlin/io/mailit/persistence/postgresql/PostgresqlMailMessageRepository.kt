package io.mailit.persistence.postgresql

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
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
           m.status ${MailMessageCol.STATUS},
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

private const val FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL = """
    SELECT m.mail_message_id ${MailMessageCol.ID},
           m.text ${MailMessageCol.TEXT},
           m.data ${MailMessageCol.DATA},
           m.subject ${MailMessageCol.SUBJECT},
           m.email_from ${MailMessageCol.EMAIL_FROM},
           m.email_to ${MailMessageCol.EMAIL_TO},
           m.created_at ${MailMessageCol.CREATED_AT},
           m.sending_started_at ${MailMessageCol.SENDING_STARTED_AT},
           m.sent_at ${MailMessageCol.SENT_AT},
           m.status ${MailMessageCol.STATUS},
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
      AND m.status = ANY($2)
    LIMIT $3"""

private const val FIND_IDS_BY_STATUSES_SQL = """
    SELECT mail_message_id 
      FROM $MAIL_MESSAGE 
     WHERE status = ANY($1)
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
           m.status ${MailMessageCol.STATUS},
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
        status,
        failed_count,
        deduplication_id)
   VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)"""

private const val UPDATE_STATUS_SQL = "UPDATE $MAIL_MESSAGE SET status = $1 WHERE mail_message_id = $2"

private const val UPDATE_STATUS_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE SET 
        status = $1, 
        sending_started_at = $2 
    WHERE mail_message_id = $3 
      AND status = ANY($4)"""

private const val UPDATE_STATUS_AND_SENT_AT_SQL = "UPDATE $MAIL_MESSAGE SET status = $1, sent_at = $2 WHERE mail_message_id = $3"

private const val UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE mail_message SET 
        status = $1, 
        failed_count = $2, 
        sending_started_at = $3 
    WHERE mail_message_id = $4"""

class PostgresqlMailMessageRepository(
    private val client: PgPool,
    private val dataSerializer: MailMessageDataSerializer,
) : MailMessageRepository {

    override suspend fun findOneWithTypeById(id: Long): MailMessage? =
        client.preparedQuery(FIND_WITH_TYPE_BY_ID_SQL).execute(Tuple.of(id))
            .onItem().transform { it.iterator() }
            .onItem().transform { if (it.hasNext()) it.next().getMailMessageWithTypeFromRow(dataSerializer) else null }
            .awaitSuspending()

    override suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(
        statuses: Collection<MailMessageStatus>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL)
            .execute(Tuple.of(sendingStartedBefore.toLocalDateTime(), statusNames, maxListSize))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<Long> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(FIND_IDS_BY_STATUSES_SQL).execute(Tuple.of(statusNames, maxListSize))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getLong("mail_message_id") }
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

    override suspend fun create(mailMessage: MailMessage): MailMessage {
        val data = dataSerializer.write(mailMessage.data)

        val argumentsArray = arrayOf(
            mailMessage.id,
            mailMessage.text,
            data,
            mailMessage.subject,
            mailMessage.emailFrom,
            mailMessage.emailTo,
            mailMessage.type.id,
            mailMessage.createdAt.toLocalDateTime(),
            mailMessage.sendingStartedAt?.toLocalDateTime(),
            mailMessage.sentAt?.toLocalDateTime(),
            mailMessage.status.name,
            mailMessage.failedCount,
            mailMessage.deduplicationId,
        )

        client.preparedQuery(INSERT_SQL)
            .execute(Tuple.from(argumentsArray))
            .onFailure(PgException::class.java).transform {
                if ((it as? PgException)?.sqlState == "23505") DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()

        return mailMessage
    }

    override suspend fun updateMessageStatus(id: Long, status: MailMessageStatus): Int =
        client.preparedQuery(UPDATE_STATUS_SQL).execute(Tuple.of(status.name, id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

    override suspend fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
        id: Long,
        statuses: Collection<MailMessageStatus>,
        status: MailMessageStatus,
        sendingStartedAt: Instant,
    ): Int {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return client.preparedQuery(UPDATE_STATUS_AND_SENDING_START_SQL).execute(Tuple.of(status.name, sendingStartedAt.toLocalDateTime(), id, statusNames))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
    }

    override suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant): Int =
        client.preparedQuery(UPDATE_STATUS_AND_SENT_AT_SQL).execute(Tuple.of(status.name, sentAt.toLocalDateTime(), id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()

    override suspend fun updateMessageStatusFailedCountAndSendingStartedTime(
        id: Long,
        status: MailMessageStatus,
        failedCount: Int,
        sendingStartedAt: Instant?,
    ): Int =
        client.preparedQuery(UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL)
            .execute(Tuple.of(status.name, failedCount, sendingStartedAt?.toLocalDateTime(), id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
}
