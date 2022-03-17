package it.mail.service.mailing

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.FAILED
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageStatus.RETRY
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.domain.MailMessageStatus.SENT
import it.mail.repository.MailMessageRepository
import it.mail.service.NotFoundException
import it.mail.support.TransactionalOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KLogging
import java.time.Instant
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SendMailMessageService(
    private val mailSender: MailSender,
    private val mailMessageRepository: MailMessageRepository,
    private val transactionalOperations: TransactionalOperations,
) {
    companion object : KLogging()

    // TODO IO dispatcher ???
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun sendMail(messageId: Long) {
        coroutineScope.launch {
            val message = getMessageForSending(messageId)

            try {
                mailSender.send(message)
                message.onSuccessfulDelivery()
            } catch (e: Exception) {
                logger.warn(e) { "Failed to send message with externalId: ${message.externalId}. Cause: ${e.message}" }
                message.onFailedDelivery()
            }
        }
    }

    private fun getMessageForSending(messageId: Long): MailMessage {
        return transactionalOperations.getWithinTransaction {
            val message = mailMessageRepository.findOneWithTypeByIdAndStatus(messageId, listOf(PENDING, RETRY))
                ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")

            message.status = SENDING
            mailMessageRepository.persist(message)

            message
        }
    }

    private fun MailMessage.onSuccessfulDelivery() {
        sentAt = Instant.now()
        status = SENT

        mailMessageRepository.persist(this)
    }

    private fun MailMessage.onFailedDelivery() {
        val maxRetries = type.maxRetriesCount ?: Int.MAX_VALUE

        if (failedCount >= maxRetries) {
            status = FAILED
            logger.error { "Max number of retries exceeded. Marking MailMessage, externalId: $externalId as failed" }
        } else {
            failedCount++
            status = RETRY
            logger.info { "Failed MailMessage, externalId: $externalId is scheduled for another delivery" }
        }

        mailMessageRepository.persist(this)
    }

    @PreDestroy
    private fun shutDown() {
        coroutineScope.cancel()
    }
}
