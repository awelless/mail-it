package io.mailit.persistence.mysql

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.common.toLocalDateTime
import io.mailit.persistence.mysql.Columns.MailMessage as MailMessageCol
import io.mailit.persistence.mysql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.mysql.Tables.MAIL_MESSAGE
import io.mailit.persistence.mysql.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.mysql.Tables.MAIL_MESSAGE_TYPE
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.mysqlclient.MySQLPool
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.mysqlclient.MySQLException
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
    WHERE m.mail_message_id = ?"""

private fun FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL(statusesNumber: Int) = """
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
    WHERE m.sending_started_at < ?
      AND m.status ${inClause(statusesNumber)}
    LIMIT ?
""".trimIndent()

private fun FIND_IDS_BY_STATUSES_SQL(statusesNumber: Int) = """
    SELECT mail_message_id 
      FROM $MAIL_MESSAGE
     WHERE status ${inClause(statusesNumber)}
     LIMIT ?"""

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
    LIMIT ? OFFSET ?"""

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
   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

private const val UPDATE_STATUS_SQL = "UPDATE $MAIL_MESSAGE SET status = ? WHERE mail_message_id = ?"

private fun UPDATE_STATUS_AND_SENDING_START_SQL(statusesNumber: Int) = """
    UPDATE $MAIL_MESSAGE SET 
        status = ?, 
        sending_started_at = ? 
    WHERE mail_message_id = ? 
      AND status ${inClause(statusesNumber)}
"""

private const val UPDATE_STATUS_AND_SENT_AT_SQL = "UPDATE $MAIL_MESSAGE SET status = ?, sent_at = ? WHERE mail_message_id = ?"

private const val UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE SET 
        status = ?, 
        failed_count = ?, 
        sending_started_at = ? 
    WHERE mail_message_id = ?"""

private fun inClause(entries: Int) = (1..entries).joinToString(
    separator = ",",
    prefix = " IN (",
    postfix = ")",
    transform = { "?" },
)

class MysqlMailMessageRepository(
    private val client: MySQLPool,
    private val dataSerializer: MailMessageDataSerializer,
) : MailMessageRepository {

    override suspend fun findOneWithTypeById(id: Long): MailMessage? =
        client.preparedQuery(FIND_WITH_TYPE_BY_ID_SQL)
            .execute(Tuple.of(id))
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

        val arguments = arrayOf(sendingStartedBefore.toLocalDateTime(), *statusNames, maxListSize)

        return client.preparedQuery(FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL(statusNames.size))
            .execute(Tuple.from(arguments))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<Long> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        val arguments = arrayOf(*statusNames, maxListSize)

        return client.preparedQuery(FIND_IDS_BY_STATUSES_SQL(statusNames.size))
            .execute(Tuple.from(arguments))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getLong("mail_message_id") }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage> {
        val offset = page * size

        return client.preparedQuery(FIND_ALL_SLICED_SQL)
            .execute(Tuple.of(size + 1, offset))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .onItem().transform { createSlice(it, page, size) }
            .awaitSuspending()
    }

    override suspend fun create(mailMessage: MailMessage): MailMessage {
        val data = dataSerializer.write(mailMessage.data)

        val arguments = arrayOf(
            mailMessage.id,
            mailMessage.text,
            data?.toBuffer(),
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
            .execute(Tuple.from(arguments))
            .onFailure(MySQLException::class.java).transform {
                if ((it as? MySQLException)?.errorCode == 1062) DuplicateUniqueKeyException(it.message, it) else it
            }
            .awaitSuspending()

        return mailMessage
    }

    override suspend fun updateMessageStatus(id: Long, status: MailMessageStatus): Int =
        client.preparedQuery(UPDATE_STATUS_SQL)
            .execute(Tuple.of(status.name, id))
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

        val arguments = arrayOf(status.name, sendingStartedAt.toLocalDateTime(), id, *statusNames)

        return client.preparedQuery(UPDATE_STATUS_AND_SENDING_START_SQL(statusNames.size))
            .execute(Tuple.from(arguments))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
    }

    override suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant): Int =
        client.preparedQuery(UPDATE_STATUS_AND_SENT_AT_SQL)
            .execute(Tuple.of(status.name, sentAt.toLocalDateTime(), id))
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
