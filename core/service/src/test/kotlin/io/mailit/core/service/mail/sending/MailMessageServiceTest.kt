package io.mailit.core.service.mail.sending

import io.mailit.core.exception.NotFoundException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus.FAILED
import io.mailit.core.model.MailMessageStatus.PENDING
import io.mailit.core.model.MailMessageStatus.RETRY
import io.mailit.core.model.MailMessageStatus.SENDING
import io.mailit.core.model.MailMessageStatus.SENT
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @InjectMockKs
    lateinit var mailMessageService: MailMessageService

    lateinit var mailMessageType: MailMessageType
    lateinit var mailMessage: MailMessage

    @BeforeEach
    fun setUp() {
        mailMessageType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun getMessageForSending_marksStatusSending() = runTest {
        coEvery {
            mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
                id = mailMessage.id,
                statuses = possibleToSendMessageStatuses,
                status = SENDING,
                sendingStartedAt = any(),
            )
        }.returns(1)
        coEvery { mailMessageRepository.findOneWithTypeById(mailMessage.id) }.returns(mailMessage)

        val actual = mailMessageService.getMessageForSending(mailMessage.id)

        assertEquals(mailMessage.id, actual.id)
    }

    @Test
    fun getMessageForSending_notFoundForMarking_marksStatusSending() = runTest {
        coEvery {
            mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
                id = mailMessage.id,
                statuses = possibleToSendMessageStatuses,
                status = SENDING,
                sendingStartedAt = any(),
            )
        }.returns(0)

        assertThrows<NotFoundException> { mailMessageService.getMessageForSending(mailMessage.id) }
    }

    @Test
    fun processSuccessfulDelivery_marksStatusSent() = runTest {
        mailMessageService.processSuccessfulDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStatusAndSentTime(eq(mailMessage.id), eq(SENT), any()) }
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusRetry() = runTest {
        mailMessage.failedCount = 0

        mailMessageService.processFailedDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStatusFailedCountAndSendingStartedTime(mailMessage.id, RETRY, 1, null) }
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusFailed() = runTest {
        mailMessageType.maxRetriesCount = 10
        mailMessage.failedCount = 10

        mailMessageService.processFailedDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStatusFailedCountAndSendingStartedTime(mailMessage.id, FAILED, 10, null) }
    }
}
