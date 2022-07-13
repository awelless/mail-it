package it.mail.domain.core.mailing

import it.mail.domain.model.MailMessage
import mu.KLogging

class HungMailsResetManager(
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    suspend fun resetAllHungMails() {
        // TODO select in batches
        val hungMails = mailMessageService.getAllHungMessages()

        logger.info { "${hungMails.size} hung mails discovered" }

        // todo parallel ?
        hungMails.map { resetHungMail(it) }

        logger.info { "Hung mails processing has finished" }
    }

    private suspend fun resetHungMail(mail: MailMessage) {
        logger.debug { "Resetting delivery for mail: ${mail.id}" }
        try {
            mailMessageService.processFailedDelivery(mail) // hung messages are considered as failed
            logger.debug { "Resetting delivery for mail: ${mail.id} finished successfully" }
        } catch (e: Exception) {
            logger.warn { "Failed to reset delivery for hung mail: ${mail.id}. Cause: ${e.message}" }
        }
    }
}
