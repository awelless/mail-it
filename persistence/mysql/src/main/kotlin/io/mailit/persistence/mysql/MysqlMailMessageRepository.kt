package io.mailit.persistence.mysql

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
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
import io.mailit.value.MailId
import io.mailit.value.MailState
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
    WHERE m.mail_message_id = ?"""

private fun FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATEES_SQL(stateesNumber: Int) = """
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
    WHERE m.sending_started_at < ?
      AND m.state ${inClause(stateesNumber)}
    LIMIT ?
""".trimIndent()

private fun FIND_IDS_BY_STATEES_SQL(stateesNumber: Int) = """
    SELECT mail_message_id 
      FROM $MAIL_MESSAGE
     WHERE state ${inClause(stateesNumber)}
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
        state,
        failed_count,
        deduplication_id)
   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

private const val UPDATE_STATE_SQL = "UPDATE $MAIL_MESSAGE SET state = ? WHERE mail_message_id = ?"

private fun UPDATE_STATE_AND_SENDING_START_SQL(stateesNumber: Int) = """
    UPDATE $MAIL_MESSAGE SET 
        state = ?, 
        sending_started_at = ? 
    WHERE mail_message_id = ? 
      AND state ${inClause(stateesNumber)}
"""

private const val UPDATE_STATE_AND_SENT_AT_SQL = "UPDATE $MAIL_MESSAGE SET state = ?, sent_at = ? WHERE mail_message_id = ?"

private const val UPDATE_STATE_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE SET 
        state = ?, 
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

    override suspend fun findOneWithTypeById(id: MailId): MailMessage? =
        client.preparedQuery(FIND_WITH_TYPE_BY_ID_SQL)
            .execute(Tuple.of(id.value))
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

        val arguments = arrayOf(sendingStartedBefore.toLocalDateTime(), *stateNames, maxListSize)

        return client.preparedQuery(FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATEES_SQL(stateNames.size))
            .execute(Tuple.from(arguments))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { it.getMailMessageWithTypeFromRow(dataSerializer) }
            .collect().asList()
            .awaitSuspending()
    }

    override suspend fun findAllIdsByStateIn(states: Collection<MailState>, maxListSize: Int): List<MailId> {
        val stateNames = states
            .map { it.name }
            .toTypedArray()

        val arguments = arrayOf(*stateNames, maxListSize)

        return client.preparedQuery(FIND_IDS_BY_STATEES_SQL(stateNames.size))
            .execute(Tuple.from(arguments))
            .onItem().transformToMulti { Multi.createFrom().iterable(it) }
            .onItem().transform { MailId(it.getLong("mail_message_id")) }
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
            mailMessage.id.value,
            mailMessage.text,
            data?.toBuffer(),
            mailMessage.subject,
            mailMessage.emailFrom?.email,
            mailMessage.emailTo.email,
            mailMessage.type.id.value,
            mailMessage.createdAt.toLocalDateTime(),
            mailMessage.sendingStartedAt?.toLocalDateTime(),
            mailMessage.sentAt?.toLocalDateTime(),
            mailMessage.state.name,
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

    override suspend fun updateMessageState(id: MailId, state: MailState): Int =
        client.preparedQuery(UPDATE_STATE_SQL)
            .execute(Tuple.of(state.name, id.value))
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

        val arguments = arrayOf(state.name, sendingStartedAt.toLocalDateTime(), id.value, *stateNames)

        return client.preparedQuery(UPDATE_STATE_AND_SENDING_START_SQL(stateNames.size))
            .execute(Tuple.from(arguments))
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
