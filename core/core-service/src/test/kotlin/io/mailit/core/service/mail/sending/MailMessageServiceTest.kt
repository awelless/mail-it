package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mailit.value.MailState.FAILED
import io.mailit.value.MailState.PENDING
import io.mailit.value.MailState.RETRY
import io.mailit.value.MailState.SENDING
import io.mailit.value.MailState.SENT
import io.mailit.value.exception.NotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    private val frozenNow = Instant.now()

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    lateinit var mailMessageService: MailMessageService

    lateinit var mailMessageType: MailMessageType
    lateinit var mailMessage: MailMessage

    @BeforeEach
    fun setUp() {
        mailMessageType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailMessageType)

        mailMessageService = MailMessageService(mailMessageRepository, Clock.fixed(frozenNow, ZoneOffset.UTC))
    }

    @Test
    fun getMessageForSending_marksStatusSending() = runTest {
        coEvery {
            mailMessageRepository.updateMessageStateAndSendingStartedTimeByIdAndStateIn(
                id = mailMessage.id,
                states = possibleToSendMessageStatuses,
                state = SENDING,
                sendingStartedAt = frozenNow,
            )
        }.returns(1)
        coEvery { mailMessageRepository.findOneWithTypeById(mailMessage.id) }.returns(mailMessage)

        val actual = mailMessageService.getMessageForSending(mailMessage.id)

        assertEquals(mailMessage.id, actual.id)
    }

    @Test
    fun getMessageForSending_notFoundForMarking_marksStatusSending() = runTest {
        coEvery {
            mailMessageRepository.updateMessageStateAndSendingStartedTimeByIdAndStateIn(
                id = mailMessage.id,
                states = possibleToSendMessageStatuses,
                state = SENDING,
                sendingStartedAt = frozenNow,
            )
        }.returns(0)

        assertThrows<NotFoundException> { mailMessageService.getMessageForSending(mailMessage.id) }
    }

    @Test
    fun processSuccessfulDelivery_marksStatusSent() = runTest {
        mailMessageService.processSuccessfulDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStateAndSentTime(mailMessage.id, SENT, frozenNow) }
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusRetry() = runTest {
        mailMessage.failedCount = 0

        mailMessageService.processFailedDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStateFailedCountAndSendingStartedTime(mailMessage.id, RETRY, 1, null) }
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusFailed() = runTest {
        mailMessageType.maxRetriesCount = 10
        mailMessage.failedCount = 10

        mailMessageService.processFailedDelivery(mailMessage)

        coVerify { mailMessageRepository.updateMessageStateFailedCountAndSendingStartedTime(mailMessage.id, FAILED, 10, null) }
    }
}
