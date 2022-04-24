package it.mail.persistence.api

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import it.mail.service.model.Slice
import java.time.Instant

interface MailMessageRepository {

    suspend fun findOneById(id: Long): MailMessage?

    suspend fun findOneWithTypeByIdAndStatus(id: Long, statuses: Collection<MailMessageStatus>): MailMessage?

    suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(statuses: Collection<MailMessageStatus>, sendingStartedBefore: Instant): List<MailMessage>

    suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>): List<Long>

    suspend fun create(mailMessage: MailMessage): MailMessage

    suspend fun updateMessageStatus(id: Long, status: MailMessageStatus)

    suspend fun updateMessageStatusAndSendingStartedTime(id: Long, status: MailMessageStatus, sendingStartedAt: Instant)

    suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant)

    suspend fun updateMessageStatusFailedCountAndSendingStartedTime(id: Long, status: MailMessageStatus, failedCount: Int, sendingStartedAt: Instant?)
}

interface MailMessageTypeRepository {

    suspend fun findById(id: Long): MailMessageType?

    suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun existsOneWithName(name: String): Boolean

    suspend fun persist(mailMessageType: MailMessageType): MailMessageType
}
