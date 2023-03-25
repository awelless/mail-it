package io.mailit.core.spi

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import java.time.Instant

interface MailMessageRepository {

    suspend fun findOneWithTypeById(id: Long): MailMessage?

    suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(
        statuses: Collection<MailMessageStatus>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage>

    suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<Long>

    suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage>

    suspend fun create(mailMessage: MailMessage): MailMessage

    suspend fun updateMessageStatus(id: Long, status: MailMessageStatus): Int

    suspend fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
        id: Long,
        statuses: Collection<MailMessageStatus>,
        status: MailMessageStatus,
        sendingStartedAt: Instant,
    ): Int

    suspend fun updateMessageStatusAndSentTime(id: Long, status: MailMessageStatus, sentAt: Instant): Int

    suspend fun updateMessageStatusFailedCountAndSendingStartedTime(id: Long, status: MailMessageStatus, failedCount: Int, sendingStartedAt: Instant?): Int
}
