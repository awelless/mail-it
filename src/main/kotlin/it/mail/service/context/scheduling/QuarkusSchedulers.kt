package it.mail.service.context.scheduling

import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP
import it.mail.service.mailing.HungMailsResetManager
import it.mail.service.mailing.UnsentMailProcessor
import java.time.Clock
import javax.inject.Singleton

@Singleton
class HungMailsResetTaskQuarkusTask(
    private val hungMailsResetManager: HungMailsResetManager,
) {

    @Scheduled(cron = "*/30 * * * * ?", concurrentExecution = SKIP)
    suspend fun run() {
        hungMailsResetManager.resetAllHungMails(Clock.systemUTC())
    }
}

@Singleton
class MailSendingTaskQuarkusTask(
    private val mailProcessor: UnsentMailProcessor,
) {

    @Scheduled(cron = "*/10 * * * * ?", concurrentExecution = SKIP)
    suspend fun run() {
        mailProcessor.processUnsentMail()
    }
}
