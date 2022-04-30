package it.mail.service.scheduling

import io.quarkus.runtime.Startup
import it.mail.service.mailing.HungMailsResetManager
import it.mail.service.mailing.UnsentMailProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Clock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration

// todo use quartz instead

@Startup
@ApplicationScoped
class HungMailsResetTaskScheduler(
    private val hungMailsResetManager: HungMailsResetManager,
    private val repeatDuration: Duration,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    @PostConstruct
    internal fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(repeatDuration)
                hungMailsResetManager.resetAllHungMails(Clock.systemUTC())
            }
        }
    }

    @PreDestroy
    internal fun tearDown() {
        coroutineScope.cancel()
    }
}

@Startup
@ApplicationScoped
class MailSendingTaskScheduler(
    private val mailProcessor: UnsentMailProcessor,
    private val repeatDuration: Duration,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    @PostConstruct
    internal fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(repeatDuration)
                mailProcessor.processUnsentMail()
            }
        }
    }

    @PreDestroy
    internal fun tearDown() {
        coroutineScope.cancel()
    }
}
