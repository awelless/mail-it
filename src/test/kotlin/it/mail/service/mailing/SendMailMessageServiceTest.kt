package it.mail.service.mailing

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState.FORCE_DELETED
import it.mail.test.createMailMessage
import it.mail.test.createMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SendMailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailSender: MailSender

    @RelaxedMockK
    lateinit var mailMessageService: MailMessageService

    @InjectMockKs
    lateinit var sendService: SendMailMessageService

    lateinit var mailMessage: MailMessage
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailMessageType = createMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun sendMail_withSuccess() = runTest {
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        sendService.sendMail(mailMessage.id).join()

        coVerify(exactly = 1) { mailSender.send(mailMessage) }
        coVerify(exactly = 1) { mailMessageService.processSuccessfulDelivery(mailMessage) }
    }

    @Test
    fun sendMail_withFailure() = runTest {
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)
        coEvery { mailSender.send(any()) }.throws(Exception())

        sendService.sendMail(mailMessage.id).join()

        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mailMessage) }
    }

    @Test
    fun sendMail_whenTypeIsForceDeleted_cancelSending() = runTest {
        mailMessageType.state = FORCE_DELETED
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        sendService.sendMail(mailMessage.id).join()

        coVerify(exactly = 1) { mailMessageService.processMessageTypeForceDeletion(mailMessage) }
        coVerify(exactly = 0) { mailSender.send(any()) }
    }
}
