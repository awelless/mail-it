package io.mailit.core.service.mail.sending

import io.mailit.value.MailId
import io.mailit.value.MailTypeState.FORCE_DELETED
import mu.KLogging

class SendMailMessageService(
    private val mailSender: MailSender,
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    suspend fun sendMail(messageId: MailId) {
        logger.debug { "Sending mail: $messageId" }

        val message = mailMessageService.getMessageForSending(messageId)

        if (message.type.state == FORCE_DELETED) {
            mailMessageService.processMessageTypeForceDeletion(message)
            return
        }

        try {
            mailSender.send(message)
            logger.debug { "Successfully sent message: ${message.id}" }

            mailMessageService.processSuccessfulDelivery(message)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to send message: ${message.id}. Cause: ${e.message}" }
            mailMessageService.processFailedDelivery(message)
        }
    }
}
