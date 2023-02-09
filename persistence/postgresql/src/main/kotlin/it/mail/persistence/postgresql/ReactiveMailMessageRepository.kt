package it.mail.persistence.postgresql

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Tuple
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus
import it.mail.core.model.Slice
import it.mail.core.spi.MailMessageRepository
import it.mail.persistence.common.createSlice
import it.mail.persistence.common.id.IdGenerator
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.common.toLocalDateTime
import java.time.Instant

// todo unify queries

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
    WHERE m.mail_message_id = $1"""

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
    WHERE m.sending_started_at < $1
      AND m.status = ANY($2)
    LIMIT $3"""

private const val FIND_IDS_BY_STATUSES_SQL = """
    SELECT mail_message_id 
      FROM mail_message 
     WHERE status = ANY($1)
     LIMIT $2"""

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
    LIMIT $1 OFFSET $2"""

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
   VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)"""

private const val UPDATE_STATUS_SQL = "UPDATE mail_message SET status = $1 WHERE mail_message_id = $2"

private const val UPDATE_STATUS_AND_SENDING_START_SQL = """
    UPDATE mail_message SET 
        status = $1, 
        sending_started_at = $2 
    WHERE mail_message_id = $3 
      AND status = ANY($4)"""

private const val UPDATE_STATUS_AND_SENT_AT_SQL = "UPDATE mail_message SET status = $1, sent_at = $2 WHERE mail_message_id = $3"

private const val UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL = """
    UPDATE mail_message SET 
        status = $1, 
        failed_count = $2, 
        sending_started_at = $3 
    WHERE mail_message_id = $4"""

class ReactiveMailMessageRepository(
    private val idGenerator: IdGenerator,
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
        maxListSize: Int
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
        val id = idGenerator.generateId()
        val data = dataSerializer.write(mailMessage.data)

        val argumentsArray = arrayOf(
            id,
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
        )

        client.preparedQuery(INSERT_SQL).execute(Tuple.from(argumentsArray))
            .awaitSuspending()

        mailMessage.id = id
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
        sendingStartedAt: Instant
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
        sendingStartedAt: Instant?
    ): Int =
        client.preparedQuery(UPDATE_STATUS_FAILED_COUNT_AND_SENDING_START_SQL)
            .execute(Tuple.of(status.name, failedCount, sendingStartedAt?.toLocalDateTime(), id))
            .onItem().transform { it.rowCount() }
            .awaitSuspending()
}
