package it.mail.service.mailing

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.CANCELED
import it.mail.domain.MailMessageStatus.FAILED
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageStatus.RETRY
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.domain.MailMessageStatus.SENT
import it.mail.repository.MailMessageRepository
import it.mail.service.NotFoundException
import mu.KLogging
import java.time.Instant
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object : KLogging()

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    fun getAllIdsOfPossibleToSentMessages(): List<Long> =
        mailMessageRepository.findAllIdsByStatusIn(possibleToSendMessageStatuses)

    @Transactional
    // TODO rewrite as a suspend method
    fun getMessageForSending(messageId: Long): MailMessage {
        val message = mailMessageRepository.findOneWithTypeByIdAndStatus(messageId, possibleToSendMessageStatuses)
            ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")

        return message.apply {
            status = SENDING
            sendingStartedAt = Instant.now()

            mailMessageRepository.persist(this)
        }
    }

    fun processSuccessfulDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            sentAt = Instant.now()
            status = SENT
        }

        mailMessageRepository.persist(mailMessage)
    }

    fun processFailedDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            val maxRetries = type.maxRetriesCount ?: Int.MAX_VALUE

            if (failedCount >= maxRetries) {
                status = FAILED
                logger.error { "Max number of retries exceeded. Marking MailMessage, externalId: $externalId as failed" }
            } else {
                failedCount++
                status = RETRY
                logger.info { "Failed MailMessage, externalId: $externalId is scheduled for another delivery" }
            }

            sendingStartedAt = null
        }

        mailMessageRepository.persist(mailMessage)
    }

    fun processMessageTypeForceDeletion(mailMessage: MailMessage) {
        mailMessage.status = CANCELED
        mailMessageRepository.persist(mailMessage)
    }
}
