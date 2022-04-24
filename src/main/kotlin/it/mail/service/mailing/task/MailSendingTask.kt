package it.mail.service.mailing.task

import io.quarkus.runtime.Startup
import it.mail.service.mailing.MailMessageService
import it.mail.service.mailing.SendMailMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration.Companion.seconds

@ApplicationScoped
class MailSendingTaskProcessor(
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

@Startup
@ApplicationScoped
class MailSendingTaskScheduler(
    private val taskProcessor: MailSendingTaskProcessor,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    private val delayDuration = 10.seconds

    @PostConstruct
    internal fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(delayDuration)
                taskProcessor.processUnsentMail()
            }
        }
    }

    @PreDestroy
    internal fun tearDown() {
        coroutineScope.cancel()
    }
}
