package io.mailit.persistence.h2

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.h2.Columns.MailMessage as MailMessageCol
import io.mailit.persistence.h2.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.h2.Tables.MAIL_MESSAGE
import io.mailit.persistence.h2.Tables.MAIL_MESSAGE_TEMPLATE
import io.mailit.persistence.h2.Tables.MAIL_MESSAGE_TYPE
import io.mailit.value.MailId
import java.sql.SQLException
import java.time.Instant
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.h2.api.ErrorCode

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
    WHERE m.sending_started_at < ?
      AND m.status IN (SELECT * FROM TABLE(x VARCHAR = ?))
    LIMIT ?"""

private const val FIND_IDS_BY_STATUSES_SQL = """
    SELECT mail_message_id 
      FROM $MAIL_MESSAGE 
     WHERE status IN (SELECT * FROM TABLE(x VARCHAR = ?)) 
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

private const val UPDATE_STATUS_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE 
       SET status = ?, 
           sending_started_at = ? 
     WHERE mail_message_id = ? 
       AND status IN (SELECT * FROM TABLE(x VARCHAR = ?))"""

private const val UPDATE_STATUS_AND_SENT_AT_SQL = "UPDATE $MAIL_MESSAGE SET status = ?, sent_at = ? WHERE mail_message_id = ?"

private const val UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE $MAIL_MESSAGE SET 
        status = ?, 
        failed_count = ?, 
        sending_started_at = ? 
    WHERE mail_message_id = ?"""

class H2MailMessageRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
    private val dataSerializer: MailMessageDataSerializer,
) : MailMessageRepository {

    private val singleMapper = SingleResultSetMapper { it.getMailMessageWithTypeFromRow(dataSerializer) }
    private val multipleMapper = MultipleResultSetMapper { it.getMailMessageWithTypeFromRow(dataSerializer) }
    private val multipleIdsMapper = MultipleResultSetMapper { MailId(it.getLong(1)) }

    override suspend fun findOneWithTypeById(id: MailId): MailMessage? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_WITH_TYPE_BY_ID_SQL,
                this.singleMapper,
                id.value,
            )
        }

    override suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(
        statuses: Collection<MailMessageStatus>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL,
                multipleMapper,
                sendingStartedBefore,
                statusNames,
                maxListSize,
            )
        }
    }

    override suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<MailId> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_IDS_BY_STATUSES_SQL,
                multipleIdsMapper,
                statusNames,
                maxListSize,
            )
        }
    }

    override suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage> {
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

    override suspend fun create(mailMessage: MailMessage): MailMessage {
        val data = dataSerializer.write(mailMessage.data)

        try {
            dataSource.connection.use {
                val dataBlob = data?.let { bytes -> it.createBlob().apply { setBytes(1, bytes) } }

                val params = arrayOf(
                    mailMessage.id.value,
                    mailMessage.text,
                    dataBlob,
                    mailMessage.subject,
                    mailMessage.emailFrom?.email,
                    mailMessage.emailTo.email,
                    mailMessage.type.id,
                    mailMessage.createdAt,
                    mailMessage.sendingStartedAt,
                    mailMessage.sentAt,
                    mailMessage.status.name,
                    mailMessage.failedCount,
                    mailMessage.deduplicationId,
                )

                queryRunner.update(it, INSERT_SQL, *params)
            }
        } catch (e: SQLException) {
            // todo replace with custom exception handler?
            throw if (e.errorCode == ErrorCode.DUPLICATE_KEY_1) {
                DuplicateUniqueKeyException(e.message, e)
            } else {
                e
            }
        }

        return mailMessage
    }

    override suspend fun updateMessageStatus(id: MailId, status: MailMessageStatus): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_SQL,
                status.name,
                id.value,
            )
        }

    override suspend fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
        id: MailId,
        statuses: Collection<MailMessageStatus>,
        status: MailMessageStatus,
        sendingStartedAt: Instant,
    ): Int {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_AND_SENDING_START_SQL,
                status.name,
                sendingStartedAt,
                id.value,
                statusNames,
            )
        }
    }

    override suspend fun updateMessageStatusAndSentTime(id: MailId, status: MailMessageStatus, sentAt: Instant): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_AND_SENT_AT_SQL,
                status.name,
                sentAt,
                id.value,
            )
        }

    override suspend fun updateMessageStatusFailedCountAndSendingStartedTime(
        id: MailId,
        status: MailMessageStatus,
        failedCount: Int,
        sendingStartedAt: Instant?,
    ): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL,
                status.name,
                failedCount,
                sendingStartedAt,
                id.value,
            )
        }
}
