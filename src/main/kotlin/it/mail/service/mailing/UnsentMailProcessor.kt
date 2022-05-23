package it.mail.service.mailing

import kotlinx.coroutines.joinAll
import mu.KLogging

class UnsentMailProcessor(
    private val mailMessageService: MailMessageService,
    private val sendService: SendMailMessageService,
) {
    companion object : KLogging()

    suspend fun processUnsentMail() {
        // TODO select in batches
        val messageIds = mailMessageService.getAllIdsOfPossibleToSentMessages()

        logger.info { "Processing ${messageIds.size} unsent messages" }

        val jobs = messageIds.map { sendService.sendMail(it) }

        try {
            jobs.joinAll()
            logger.info { "Processing of unsent messages has finished" }
        } catch (e: Exception) {
            logger.warn { "Processing of unsent messages has finished with exception. Cause message: ${e.message}. Cause: $e" }
        }
    }
}
