package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageType
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mailit.value.MailTypeState.FORCE_DELETED
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
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
        mailMessageType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun sendMail_withSuccess() = runTest {
        // given
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        // when
        sendService.sendMail(mailMessage.id)

        // then
        coVerify(exactly = 1) { mailSender.send(mailMessage) }
        coVerify(exactly = 1) { mailMessageService.processSuccessfulDelivery(mailMessage) }
    }

    @Test
    fun sendMail_withFailure() = runTest {
        // given
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)
        coEvery { mailSender.send(any()) }.throws(Exception())

        // when
        sendService.sendMail(mailMessage.id)

        // then
        coVerify(exactly = 1) { mailMessageService.processFailedDelivery(mailMessage) }
    }

    @Test
    fun sendMail_whenTypeIsForceDeleted_cancelSending() = runTest {
        // given
        mailMessageType.state = FORCE_DELETED
        coEvery { mailMessageService.getMessageForSending(mailMessage.id) }.returns(mailMessage)

        // when
        sendService.sendMail(mailMessage.id)

        // then
        coVerify(exactly = 1) { mailMessageService.processMessageTypeForceDeletion(mailMessage) }
        coVerify(exactly = 0) { mailSender.send(any()) }
    }
}
