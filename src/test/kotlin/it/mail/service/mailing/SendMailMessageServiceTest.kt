package it.mail.service.mailing

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import it.mail.domain.MailMessage
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

    @BeforeEach
    fun setUp() {
        val mailMessageType = createMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun sendMail_withSuccess() = runTest {
        every { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        sendService.sendMail(mailMessage.id).join()

        coVerify(exactly = 1) { mailSender.send(mailMessage) }
        verify(exactly = 1) { mailMessageService.processSuccessfulDelivery(mailMessage) }
    }

    @Test
    fun sendMail_withFailure() = runTest {
        every { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)
        coEvery { mailSender.send(any()) }.throws(Exception())

        sendService.sendMail(mailMessage.id).join()

        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mailMessage) }
    }
}
