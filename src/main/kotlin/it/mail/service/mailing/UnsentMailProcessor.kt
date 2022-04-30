package it.mail.service.mailing

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

        messageIds.forEach(sendService::sendMail)
    }
}
