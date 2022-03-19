package it.mail.service.mailing.task

import it.mail.service.mailing.MailMessageService
import it.mail.service.mailing.SendMailMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration.Companion.seconds

@ApplicationScoped
class MailSendingTaskProcessor(
    private val mailMessageService: MailMessageService,
    private val sendService: SendMailMessageService,
) {
    fun processUnsentMail() {
        mailMessageService.getAllIdsOfPossibleToSentMessages()
            .forEach(sendService::sendMail)
    }
}

@ApplicationScoped
class MailSendingTaskScheduler(
    private val taskProcessor: MailSendingTaskProcessor,
) {
    // TODO single threaded dispatcher here ?
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val delayDuration = 10.seconds

    @PostConstruct
    private fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(delayDuration)
                taskProcessor.processUnsentMail()
            }
        }
    }

    @PreDestroy
    private fun tearDown() {
        coroutineScope.cancel()
    }
}
