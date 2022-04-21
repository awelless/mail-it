package it.mail.service.mailing.task

import io.quarkus.runtime.Startup
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.repository.MailMessageRepository
import it.mail.service.mailing.MailMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import java.time.Clock
import java.time.Instant
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ApplicationScoped
class HungMailsResetManager(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageService: MailMessageService,
) {
    companion object : KLogging()

    private val hungMessageStatuses = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    fun resetAllHungMails(clock: Clock) {
        val sendingStartedBefore = Instant.now(clock).minus(hungMessageDuration)
        // TODO select in batches
        val hungMails = mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(hungMessageStatuses, sendingStartedBefore)

        logger.info { "${hungMails.size} hung mails discovered" }

        hungMails.forEach { resetHungMail(it) }
    }

    private fun resetHungMail(mail: MailMessage) {
        logger.debug { "Resetting delivery for mail with externalId: ${mail.externalId}" }
        try {
            mailMessageService.processFailedDelivery(mail) // hung messages are considered as failed
            logger.debug { "Resetting delivery for mail with externalId: ${mail.externalId} finished successfully" }
        } catch (e: Exception) {
            logger.warn { "Failed to reset delivery for hung mail with externalId: ${mail.externalId}. Cause: ${e.message}" }
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
    private fun setUp() {
        coroutineScope.launch {
            while (true) {
                delay(delayDuration)
                hungMailsResetManager.resetAllHungMails(Clock.systemUTC())
            }
        }
    }

    @PreDestroy
    private fun tearDown() {
        coroutineScope.cancel()
    }
}
