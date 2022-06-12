package it.mail.core.mailing

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.quarkus.mailer.Mail
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageType
import it.mail.core.model.MailMessageTypeState.FORCE_DELETED
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SendMailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailFactory: MailFactory

    @RelaxedMockK
    lateinit var mailSender: MailSender

    @RelaxedMockK
    lateinit var mailMessageService: MailMessageService

    @SpyK
    var coroutineScope = CoroutineScope(Dispatchers.Default)

    @InjectMockKs
    lateinit var sendService: SendMailMessageService

    lateinit var mailMessage: MailMessage
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailMessageType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun sendMail_withSuccess() = runTest {
        // given
        val mail = Mail.withText(mailMessage.emailTo, mailMessage.subject, mailMessage.text)

        every { mailFactory.create(mailMessage) }.returns(mail)
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        // when
        sendService.sendMail(mailMessage.id).join()

        // then
        coVerify(exactly = 1) { mailSender.send(mail) }
        coVerify(exactly = 1) { mailMessageService.processSuccessfulDelivery(mailMessage) }
    }

    @Test
    fun sendMail_withFailure() = runTest {
        // given
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)
        coEvery { mailSender.send(any()) }.throws(Exception())

        // when
        sendService.sendMail(mailMessage.id).join()

        // then
        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mailMessage) }
    }

    @Test
    fun sendMail_whenTypeIsForceDeleted_cancelSending() = runTest {
        // given
        mailMessageType.state = FORCE_DELETED
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        // when
        sendService.sendMail(mailMessage.id).join()

        // then
        coVerify(exactly = 1) { mailMessageService.processMessageTypeForceDeletion(mailMessage) }
        coVerify(exactly = 0) { mailSender.send(any()) }
    }
}
