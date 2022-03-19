package it.mail.service.mailing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KLogging
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SendMailMessageService(
    private val mailSender: MailSender,
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun sendMail(messageId: Long): Job {
        return coroutineScope.launch {
            val message = mailMessageService.getMessageForSending(messageId)

            try {
                mailSender.send(message)
                mailMessageService.processSuccessfulDelivery(message)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to send message with externalId: ${message.externalId}. Cause: ${e.message}" }
                mailMessageService.processFailedDelivery(message)
            }
        }
    }

    @PreDestroy
    private fun shutDown() {
        coroutineScope.cancel()
    }
}
