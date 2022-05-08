package it.mail.persistence.api

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import it.mail.domain.Slice
import java.time.Instant

interface MailMessageRepository {

    suspend fun findOneById(id: Long): MailMessage?

    suspend fun findOneWithTypeByIdAndStatus(id: Long, statuses: Collection<MailMessageStatus>): MailMessage?

    suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(statuses: Collection<MailMessageStatus>, sendingStartedBefore: Instant): List<MailMessage>

    suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>): List<Long>

    suspend fun create(mailMessage: MailMessage): MailMessage

    suspend fun updateMessageStatus(id: Long, status: MailMessageStatus): Int

    suspend fun updateMessageStatusAndSendingStartedTime(id: Long, status: MailMessageStatus, sendingStartedAt: Instant): Int

    suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant): Int

    suspend fun updateMessageStatusFailedCountAndSendingStartedTime(id: Long, status: MailMessageStatus, failedCount: Int, sendingStartedAt: Instant?): Int
}

interface MailMessageTypeRepository {

    suspend fun findById(id: Long): MailMessageType?

    suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun existsOneWithName(name: String): Boolean

    // todo replace with separate queries
    suspend fun persist(mailMessageType: MailMessageType): MailMessageType
}
