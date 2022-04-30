package it.mail.service.mailing

import it.mail.domain.MailMessage
import mu.KLogging
import java.time.Clock

class HungMailsResetManager(
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    suspend fun resetAllHungMails(clock: Clock) {
        // TODO select in batches
        val hungMails = mailMessageService.getAllHungMessages()

        logger.info { "${hungMails.size} hung mails discovered" }

        hungMails.forEach { resetHungMail(it) }
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
