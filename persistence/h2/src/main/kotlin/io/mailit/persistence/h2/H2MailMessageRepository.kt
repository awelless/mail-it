package io.mailit.persistence.h2

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository
import io.mailit.persistence.common.createSlice
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler

private const val FIND_WITH_TYPE_BY_ID_SQL = """
    SELECT m.mail_message_id m_mail_message_id,
           m.text m_text,
           m.data m_data,
           m.subject m_subject,
           m.email_from m_email_from,
           m.email_to m_email_to,
           m.created_at m_created_at,
           m.sending_started_at m_sending_started_at,
           m.sent_at m_sent_at,
           m.status m_status,
           m.failed_count m_failed_count,
           mt.mail_message_type_id mt_mail_message_type_id,
           mt.name mt_name,
           mt.description mt_description,
           mt.max_retries_count mt_max_retries_count,
           mt.state mt_state,
           mt.created_at mt_created_at,
           mt.updated_at mt_updated_at,
           mt.content_type mt_content_type,
           mt.template_engine mt_template_engine,
           mt.template mt_template
    FROM mail_message m
    INNER JOIN mail_message_type mt ON m.mail_message_type_id = mt.mail_message_type_id
    WHERE m.mail_message_id = ?"""

private const val FIND_WITH_TYPE_BY_SENDING_STARTED_BEFORE_AND_STATUSES_SQL = """
    SELECT m.mail_message_id m_mail_message_id,
           m.text m_text,
           m.data m_data,
           m.subject m_subject,
           m.email_from m_email_from,
           m.email_to m_email_to,
           m.created_at m_created_at,
           m.sending_started_at m_sending_started_at,
           m.sent_at m_sent_at,
           m.status m_status,
           m.failed_count m_failed_count,
           mt.mail_message_type_id mt_mail_message_type_id,
           mt.name mt_name,
           mt.description mt_description,
           mt.max_retries_count mt_max_retries_count,
           mt.state mt_state,
           mt.created_at mt_created_at,
           mt.updated_at mt_updated_at,
           mt.content_type mt_content_type,
           mt.template_engine mt_template_engine,
           mt.template mt_template
    FROM mail_message m
    INNER JOIN mail_message_type mt ON m.mail_message_type_id = mt.mail_message_type_id
    WHERE m.sending_started_at < ?
      AND m.status IN (SELECT * FROM TABLE(x VARCHAR = ?))
    LIMIT ?"""

private const val FIND_IDS_BY_STATUSES_SQL = """
    SELECT mail_message_id 
      FROM mail_message 
     WHERE status IN (SELECT * FROM TABLE(x VARCHAR = ?)) 
     LIMIT ?"""

private const val FIND_ALL_SLICED_SQL = """
    SELECT m.mail_message_id m_mail_message_id,
           m.text m_text,
           m.data m_data,
           m.subject m_subject,
           m.email_from m_email_from,
           m.email_to m_email_to,
           m.created_at m_created_at,
           m.sending_started_at m_sending_started_at,
           m.sent_at m_sent_at,
           m.status m_status,
           m.failed_count m_failed_count,
           mt.mail_message_type_id mt_mail_message_type_id,
           mt.name mt_name,
           mt.description mt_description,
           mt.max_retries_count mt_max_retries_count,
           mt.state mt_state,
           mt.created_at mt_created_at,
           mt.updated_at mt_updated_at,
           mt.content_type mt_content_type,
           mt.template_engine mt_template_engine,
           mt.template mt_template
     FROM mail_message m
    INNER JOIN mail_message_type mt ON m.mail_message_type_id = mt.mail_message_type_id
    ORDER BY m_mail_message_id DESC
    LIMIT ? OFFSET ?"""

private const val INSERT_SQL = """
   INSERT INTO mail_message(
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
        failed_count)
   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

private const val UPDATE_STATUS_SQL = "UPDATE mail_message SET status = ? WHERE mail_message_id = ?"

private const val UPDATE_STATUS_AND_SENDING_START_SQL = """
    UPDATE mail_message 
       SET status = ?, 
           sending_started_at = ? 
     WHERE mail_message_id = ? 
       AND status IN (SELECT * FROM TABLE(x VARCHAR = ?))"""

private const val UPDATE_STATUS_AND_SENT_AT_SQL = "UPDATE mail_message SET status = ?, sent_at = ? WHERE mail_message_id = ?"

private const val UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE mail_message SET 
        status = ?, 
        failed_count = ?, 
        sending_started_at = ? 
    WHERE mail_message_id = ?"""

class H2MailMessageRepository(
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
    private val dataSerializer: MailMessageDataSerializer,
) : MailMessageRepository {

    private val singleMailWithTypeMapper = SingleMailMessageWithTypeResultSetMapper(dataSerializer)
    private val multipleMailWithTypeMapper = MultipleMailMessagesWithTypeResultSetMapper(dataSerializer)

    override suspend fun findOneWithTypeById(id: Long): MailMessage? =
        dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_WITH_TYPE_BY_ID_SQL,
                singleMailWithTypeMapper,
                id,
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
                multipleMailWithTypeMapper,
                sendingStartedBefore,
                statusNames,
                maxListSize,
            )
        }
    }

    override suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<Long> {
        val statusNames = statuses
            .map { it.name }
            .toTypedArray()

        return dataSource.connection.use {
            queryRunner.query(
                it,
                FIND_IDS_BY_STATUSES_SQL,
                IDS_MAPPER,
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
                multipleMailWithTypeMapper,
                size + 1,
                offset,
            )
        }

        return createSlice(content, page, size)
    }

    override suspend fun create(mailMessage: MailMessage): MailMessage {
        val data = dataSerializer.write(mailMessage.data)

        dataSource.connection.use {
            val dataBlob = it.createBlob()
            dataBlob.setBytes(1, data)

            queryRunner.update(
                it, INSERT_SQL,
                mailMessage.id, mailMessage.text, dataBlob, mailMessage.subject, mailMessage.emailFrom, mailMessage.emailTo, mailMessage.type.id,
                mailMessage.createdAt, mailMessage.sendingStartedAt, mailMessage.sentAt, mailMessage.status.name, mailMessage.failedCount,
            )
        }

        return mailMessage
    }

    override suspend fun updateMessageStatus(id: Long, status: MailMessageStatus): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_SQL,
                status.name,
                id,
            )
        }

    override suspend fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
        id: Long,
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
                id,
                statusNames,
            )
        }
    }

    override suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant): Int =
        dataSource.connection.use {
            queryRunner.update(
                it,
                UPDATE_STATUS_AND_SENT_AT_SQL,
                status.name,
                sentAt,
                id,
            )
        }

    override suspend fun updateMessageStatusFailedCountAndSendingStartedTime(
        id: Long,
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
                id,
            )
        }
}

/**
 * Used to extract single mail with type. Thread safe
 */
private class SingleMailMessageWithTypeResultSetMapper(
    private val dataSerializer: MailMessageDataSerializer,
) : ResultSetHandler<MailMessage?> {

    override fun handle(rs: ResultSet?): MailMessage? =
        if (rs?.next() == true) {
            rs.getMailMessageWithTypeFromRow(dataSerializer)
        } else {
            null
        }
}

/**
 * Used to extract list of mails with type. Thread safe
 */
private class MultipleMailMessagesWithTypeResultSetMapper(
    private val dataSerializer: MailMessageDataSerializer,
) : ResultSetHandler<List<MailMessage>> {

    override fun handle(rs: ResultSet?): List<MailMessage> {
        if (rs == null) {
            return ArrayList()
        }

        val mailTypes = ArrayList<MailMessage>()
        while (rs.next()) {
            mailTypes.add(rs.getMailMessageWithTypeFromRow(dataSerializer))
        }
        return mailTypes
    }
}
