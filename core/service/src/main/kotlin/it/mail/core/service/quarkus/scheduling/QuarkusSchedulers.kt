package it.mail.core.service.quarkus.scheduling

import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.PROCEED
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP
import it.mail.core.service.mailing.HungMailsResetManager
import it.mail.core.service.mailing.UnsentMailProcessor
import javax.inject.Singleton

const val EACH_10_SECONDS = "*/10 * * * * ?"
const val EACH_30_SECONDS = "*/30 * * * * ?"

@Singleton
class HungMailsResetTaskQuarkusTask(
    private val hungMailsResetManager: HungMailsResetManager,
) {

    @Scheduled(cron = EACH_30_SECONDS, concurrentExecution = SKIP)
    suspend fun run() {
        hungMailsResetManager.resetAllHungMails()
    }
}

@Singleton
class MailSendingTaskQuarkusTask(
    private val mailProcessor: UnsentMailProcessor,
) {

    @Scheduled(cron = EACH_10_SECONDS, concurrentExecution = PROCEED)
    suspend fun run() {
        mailProcessor.processUnsentMail()
    }
}
