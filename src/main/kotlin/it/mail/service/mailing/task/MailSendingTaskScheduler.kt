package it.mail.service.mailing.task

import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageStatus.RETRY
import it.mail.repository.MailMessageRepository
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
    private val mailMessageRepository: MailMessageRepository,
    private val sendService: SendMailMessageService,
) {

    private val unsentMessageStatuses = listOf(PENDING, RETRY)

    fun processUnsentMail() {
        mailMessageRepository.findAllIdsByStatusIn(unsentMessageStatuses)
            .forEach(sendService::sendMail)
    }
}

@ApplicationScoped
class MailSendingTaskScheduler(
    private val taskProcessor: MailSendingTaskProcessor,
) {
    // TODO single threaded dispatcher here ?
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val delayDuration = 30.seconds

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
