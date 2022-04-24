package it.mail.service.mailing.task

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.service.mailing.MailMessageService
import it.mail.test.createMailMessage
import it.mail.test.createMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@ExtendWith(MockKExtension::class)
class HungMailsResetManagerTest {

    private val hungMessageStatuses = listOf(SENDING)

    @RelaxedMockK
    lateinit var mailMessageService: MailMessageService

    @InjectMockKs
    lateinit var hungMailsResetManager: HungMailsResetManager

    lateinit var mail1: MailMessage
    lateinit var mail2: MailMessage

    @BeforeEach
    fun setUp() {
        val type = createMailMessageType()
        mail1 = createMailMessage(type)
        mail2 = createMailMessage(type)
    }

    @Test
    fun resetAllHungMails_considersThemAsFailed() = runTest {
        // given
        val clock = Clock.fixed(Instant.parse("2022-04-21T21:00:00Z"), UTC)
        val hungMessagesBefore = Instant.now(clock).minus(2.minutes.toJavaDuration())

        coEvery { mailMessageService.getAllHungMessages() }.returns(listOf(mail1, mail2))

        // when
        hungMailsResetManager.resetAllHungMails(clock)

        // then
        coVerify { mailMessageService.processFailedDelivery(mail1) }
        coVerify { mailMessageService.processFailedDelivery(mail2) }
    }
}
