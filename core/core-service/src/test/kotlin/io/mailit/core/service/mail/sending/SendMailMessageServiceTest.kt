package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState.FORCE_DELETED
import io.mailit.core.service.fake.mailer.FailingMailSender
import io.mailit.core.service.fake.mailer.MailSenderSpy
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SendMailMessageServiceTest {

    val mailFactory = MailFactory(mockk())
    val mailSender = MailSenderSpy()

    @RelaxedMockK
    lateinit var mailMessageService: MailMessageService

    lateinit var mailMessage: MailMessage
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailMessageType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailMessageType)

        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)
    }

    @Test
    fun sendMail_withSuccess() = runTest {
        // given
        val sendService = SendMailMessageService(mailFactory, mailSender, mailMessageService)

        // when
        sendService.sendMail(mailMessage.id)

        // then
        assertEquals(1, mailSender.sentMails.size)
        coVerify(exactly = 1) { mailMessageService.processSuccessfulDelivery(mailMessage) }
    }

    @Test
    fun sendMail_withFailure() = runTest {
        // given
        val sendService = SendMailMessageService(mailFactory, FailingMailSender, mailMessageService)

        // when
        sendService.sendMail(mailMessage.id)

        // then
        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mailMessage) }
    }

    @Test
    fun sendMail_whenTypeIsForceDeleted_cancelSending() = runTest {
        // given
        mailMessageType.state = FORCE_DELETED

        val sendService = SendMailMessageService(mailFactory, mailSender, mailMessageService)

        // when
        sendService.sendMail(mailMessage.id)

        // then
        coVerify(exactly = 1) { mailMessageService.processMessageTypeForceDeletion(mailMessage) }
        assertTrue(mailSender.sentMails.isEmpty())
    }
}
