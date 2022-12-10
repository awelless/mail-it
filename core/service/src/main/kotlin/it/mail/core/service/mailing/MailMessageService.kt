package it.mail.core.service.mailing

import it.mail.core.exception.NotFoundException
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus.CANCELED
import it.mail.core.model.MailMessageStatus.FAILED
import it.mail.core.model.MailMessageStatus.PENDING
import it.mail.core.model.MailMessageStatus.RETRY
import it.mail.core.model.MailMessageStatus.SENDING
import it.mail.core.model.MailMessageStatus.SENT
import it.mail.core.spi.MailMessageRepository
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import mu.KLogging

class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object : KLogging()

    private val hungMessageStatuses = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    suspend fun getHungMessages(maxListSize: Int) =
        mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(hungMessageStatuses, Instant.now().minus(hungMessageDuration), maxListSize)

    suspend fun getAllIdsOfPossibleToSentMessages(maxListSize: Int) =
        mailMessageRepository.findAllIdsByStatusIn(possibleToSendMessageStatuses, maxListSize)

    suspend fun getMessageForSending(messageId: Long): MailMessage {
        val messagesUpdated = mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
            id = messageId,
            statuses = possibleToSendMessageStatuses,
            status = SENDING,
            sendingStartedAt = Instant.now(),
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
            sentAt = Instant.now()
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
