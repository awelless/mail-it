package io.mailit.core.service.mailing

import io.mailit.core.model.MailMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import mu.KLogging

class HungMailsResetManager(
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging() {
        const val BATCH_SIZE = 200
    }

    suspend fun resetAllHungMails() {
        logger.info { "Hung mails processing has started" }

        resetBatchOfHungMails() // recursive method

        logger.info { "Hung mails processing has finished" }
    }

    private tailrec suspend fun resetBatchOfHungMails() {
        val hungMails = mailMessageService.getHungMessages(BATCH_SIZE)

        logger.info { "${hungMails.size} hung mails discovered" }

        val hungMailResetTasks = with(CoroutineScope(currentCoroutineContext())) {
            hungMails.map { async { resetHungMail(it) } }
        }

        hungMailResetTasks.awaitAll()

        logger.info { "${hungMails.size} hung mails ware processed" }

        if (hungMails.size >= BATCH_SIZE) { // if batch is full, there can be more hung items
            resetBatchOfHungMails() // tail call
        }
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
