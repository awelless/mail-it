package io.mailit.core.spi

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import io.mailit.value.MailId
import java.time.Instant

interface MailMessageRepository {

    suspend fun findOneWithTypeById(id: MailId): MailMessage?

    suspend fun findAllWithTypeByStatusesAndSendingStartedBefore(
        statuses: Collection<MailMessageStatus>,
        sendingStartedBefore: Instant,
        maxListSize: Int,
    ): List<MailMessage>

    suspend fun findAllIdsByStatusIn(statuses: Collection<MailMessageStatus>, maxListSize: Int): List<MailId>

    /**
     * Returns a [Slice] of specified zero indexed [page] and [size]
     */
    suspend fun findAllSlicedDescendingIdSorted(page: Int, size: Int): Slice<MailMessage>

    suspend fun create(mailMessage: MailMessage): MailMessage

    suspend fun updateMessageStatus(id: MailId, status: MailMessageStatus): Int

    suspend fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
        id: MailId,
        statuses: Collection<MailMessageStatus>,
        status: MailMessageStatus,
        sendingStartedAt: Instant,
    ): Int

    suspend fun updateMessageStatusAndSentTime(id: MailId, status: MailMessageStatus, sentAt: Instant): Int

    suspend fun updateMessageStatusFailedCountAndSendingStartedTime(id: MailId, status: MailMessageStatus, failedCount: Int, sendingStartedAt: Instant?): Int
}
