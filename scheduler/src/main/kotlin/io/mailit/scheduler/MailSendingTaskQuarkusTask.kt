package io.mailit.scheduler

import io.mailit.core.api.scheduler.SendMails
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.PROCEED
import jakarta.inject.Singleton

@Singleton
internal class MailSendingTaskQuarkusTask(
    private val sendMails: SendMails,
) {

    // todo proceed?
    @Scheduled(cron = EVERY_10_SECONDS, concurrentExecution = PROCEED)
    internal suspend fun run() = sendMails()
}
