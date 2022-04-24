package it.mail.service.mailing

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.CANCELED
import it.mail.domain.MailMessageStatus.FAILED
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageStatus.RETRY
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.domain.MailMessageStatus.SENT
import it.mail.persistence.api.MailMessageRepository
import it.mail.service.NotFoundException
import mu.KLogging
import java.time.Instant
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@ApplicationScoped
class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object : KLogging()

    private val hungMessageStatuses = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    suspend fun getAllHungMessages(): List<MailMessage> =
        mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(hungMessageStatuses, Instant.now().minus(hungMessageDuration))

    suspend fun getAllIdsOfPossibleToSentMessages(): List<Long> =
        mailMessageRepository.findAllIdsByStatusIn(possibleToSendMessageStatuses)

    // @Transactional // todo transaction doesn't work
    suspend fun getMessageForSending(messageId: Long): MailMessage {
        val message = mailMessageRepository.findOneWithTypeByIdAndStatus(messageId, possibleToSendMessageStatuses)
            ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")

        message.apply {
            status = SENDING
            sendingStartedAt = Instant.now()
        }

        mailMessageRepository.updateMessageStatusAndSendingStartedTime(messageId, message.status, message.sendingStartedAt!!)

        return message
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
