package io.mailit.core.service.mail.sending

import io.mailit.core.exception.NotFoundException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus.CANCELED
import io.mailit.core.model.MailMessageStatus.FAILED
import io.mailit.core.model.MailMessageStatus.PENDING
import io.mailit.core.model.MailMessageStatus.RETRY
import io.mailit.core.model.MailMessageStatus.SENDING
import io.mailit.core.model.MailMessageStatus.SENT
import io.mailit.core.spi.MailMessageRepository
import io.mailit.value.MailId
import java.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import mu.KLogging

class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val clock: Clock,
) {
    companion object : KLogging()

    private val hungMessageStatuses = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    suspend fun getHungMessages(maxListSize: Int) =
        mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(hungMessageStatuses, clock.instant().minus(hungMessageDuration), maxListSize)

    suspend fun getAllIdsOfPossibleToSentMessages(maxListSize: Int) =
        mailMessageRepository.findAllIdsByStatusIn(possibleToSendMessageStatuses, maxListSize)

    suspend fun getMessageForSending(messageId: MailId): MailMessage {
        val messagesUpdated = mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
            id = messageId,
            statuses = possibleToSendMessageStatuses,
            status = SENDING,
            sendingStartedAt = clock.instant(),
        )

        if (messagesUpdated == 0) {
            throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
        }

        return mailMessageRepository.findOneWithTypeById(messageId)
            ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
    }

    suspend fun processSuccessfulDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            status = SENT
            sentAt = clock.instant()
        }

        mailMessageRepository.updateMessageStatusAndSentTime(mailMessage.id, mailMessage.status, mailMessage.sentAt!!)
    }

    suspend fun processFailedDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            val maxRetries = type.maxRetriesCount ?: Int.MAX_VALUE

            if (failedCount >= maxRetries) {
                status = FAILED
                logger.error { "Max number of retries exceeded. Marking MailMessage: $id as failed" }
            } else {
                failedCount++
                status = RETRY
                logger.info { "Failed MailMessage: $id is scheduled for another delivery" }
            }

            sendingStartedAt = null
        }

        mailMessageRepository.updateMessageStatusFailedCountAndSendingStartedTime(mailMessage.id, mailMessage.status, mailMessage.failedCount, null)
    }

    suspend fun processMessageTypeForceDeletion(mailMessage: MailMessage) {
        mailMessage.status = CANCELED

        mailMessageRepository.updateMessageStatus(mailMessage.id, mailMessage.status)
    }
}
