package it.mail.core.mailing

import it.mail.core.model.MailMessageTypeState.FORCE_DELETED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KLogging

class SendMailMessageService(
    private val mailSender: MailSender,
    private val mailMessageService: MailMessageService,
    private val coroutineScope: CoroutineScope,
) {
    companion object : KLogging()

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

    fun stop() {
        logger.info { "Stopping SendMailMessageService" }
        coroutineScope.cancel()
        logger.info { "Stopped SendMailMessageService" }
    }
}
