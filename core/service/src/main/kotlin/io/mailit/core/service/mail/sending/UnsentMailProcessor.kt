package io.mailit.core.service.mail.sending

import io.mailit.value.MailId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import mu.KLogging

class UnsentMailProcessor(
    private val mailMessageService: MailMessageService,
    private val sendService: SendMailMessageService,
) {
    companion object : KLogging() {
        const val BATCH_SIZE = 200
    }

    suspend fun processUnsentMail() {
        logger.info { "Unsent mails processing has started" }

        processUnsentMailBatch() // recursive method

        logger.info { "Unsent mails processing has finished" }
    }

    private tailrec suspend fun processUnsentMailBatch() {
        val unsentMessageIds = mailMessageService.getAllIdsOfPossibleToSentMessages(BATCH_SIZE)

        logger.info { "${unsentMessageIds.size} unsent mails discovered" }

        val mailSendTasks = with(CoroutineScope(currentCoroutineContext())) {
            unsentMessageIds.map { async { sendMail(it) } }
        }

        mailSendTasks.awaitAll()

        logger.info { "${unsentMessageIds.size} unsent mails ware processed" }

        if (unsentMessageIds.size >= BATCH_SIZE) { // if batch is full, there can be more unsent messages
            processUnsentMailBatch() // trail call
        }
    }

    private suspend fun sendMail(id: MailId) {
        try {
            sendService.sendMail(id)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to send a message: $id. Cause: ${e.message}" }
        }
    }
}
