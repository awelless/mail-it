package it.mail.service.mailing

import it.mail.domain.MailMessageTypeState.FORCE_DELETED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KLogging
import javax.annotation.PreDestroy

class SendMailMessageService(
    private val mailSender: MailSender,
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun sendMail(messageId: Long): Job {
        return coroutineScope.launch {
            sendMailSuspending(messageId)
        }
    }

    private suspend fun sendMailSuspending(messageId: Long) {
        val message = mailMessageService.getMessageForSending(messageId)

        if (message.type.state == FORCE_DELETED) {
            mailMessageService.processMessageTypeForceDeletion(message)
            return
        }

        try {
            mailSender.send(message)
            mailMessageService.processSuccessfulDelivery(message)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to send message: ${message.id}. Cause: ${e.message}" }
            mailMessageService.processFailedDelivery(message)
        }
    }

    @PreDestroy
    internal fun shutDown() {
        coroutineScope.cancel()
    }
}
