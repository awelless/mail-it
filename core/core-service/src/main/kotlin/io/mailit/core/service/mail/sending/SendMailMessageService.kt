package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessageTypeState.FORCE_DELETED
import io.mailit.core.spi.mailer.MailSender
import io.mailit.lang.flatMap
import io.mailit.value.MailId
import mu.KLogging

class SendMailMessageService(
    private val mailFactory: MailFactory,
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

        mailFactory.create(message)
            .flatMap { mailSender.send(it) }
            .onSuccess {
                logger.debug { "Successfully sent message: ${message.id}" }
                mailMessageService.processSuccessfulDelivery(message)
            }
            .onFailure {
                logger.warn(it) { "Failed to send message: ${message.id}. Cause: ${it.message}" }
                mailMessageService.processFailedDelivery(message)
            }
    }
}
