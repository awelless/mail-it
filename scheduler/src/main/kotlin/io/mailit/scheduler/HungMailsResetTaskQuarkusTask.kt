package io.mailit.scheduler

import io.mailit.core.api.scheduler.ResetHungMails
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP
import jakarta.inject.Singleton

@Singleton
internal class HungMailsResetTaskQuarkusTask(
    private val resetHungMails: ResetHungMails,
) {

    @Scheduled(cron = EVERY_30_SECONDS, concurrentExecution = SKIP)
    internal suspend fun run() = resetHungMails()
}
