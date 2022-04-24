package it.mail.service.mailing.task

import io.quarkus.runtime.Startup
import it.mail.domain.MailMessage
import it.mail.service.mailing.MailMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import java.time.Clock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration.Companion.seconds

@ApplicationScoped
class HungMailsResetManager(
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    suspend fun resetAllHungMails(clock: Clock) {
        // TODO select in batches
        val hungMails = mailMessageService.getAllHungMessages()

        logger.info { "${hungMails.size} hung mails discovered" }

        hungMails.forEach { resetHungMail(it) }
    }

    private suspend fun resetHungMail(mail: MailMessage) {
        logger.debug { "Resetting delivery for mail: ${mail.id}" }
        try {
            mailMessageService.processFailedDelivery(mail) // hung messages are considered as failed
            logger.debug { "Resetting delivery for mail: ${mail.id} finished successfully" }
        } catch (e: Exception) {
            logger.warn { "Failed to reset delivery for hung mail: ${mail.id}. Cause: ${e.message}" }
        }
    }
}

@Startup
@ApplicationScoped
class HungMailsResetTaskScheduler(
    private val hungMailsResetManager: HungMailsResetManager,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    private val delayDuration = 30.seconds

    @PostConstruct
    internal fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(delayDuration)
                hungMailsResetManager.resetAllHungMails(Clock.systemUTC())
            }
        }
    }

    @PreDestroy
    internal fun tearDown() {
        coroutineScope.cancel()
    }
}
