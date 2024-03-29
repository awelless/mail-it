package io.mailit.core.service.quarkus.scheduling

import io.mailit.core.service.mail.sending.HungMailsResetManager
import io.mailit.core.service.mail.sending.UnsentMailProcessor
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.PROCEED
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP
import jakarta.inject.Singleton

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
