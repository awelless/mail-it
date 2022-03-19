package it.mail.service.mailing

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.FAILED
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageStatus.RETRY
import it.mail.domain.MailMessageStatus.SENDING
import it.mail.domain.MailMessageStatus.SENT
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageRepository
import it.mail.test.createMailMessage
import it.mail.test.createMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @InjectMockKs
    lateinit var mailMessageService: MailMessageService

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailMessageType: MailMessageType
    lateinit var mailMessage: MailMessage

    @BeforeEach
    fun setUp() {
        mailMessageType = createMailMessageType()
        mailMessage = createMailMessage(mailMessageType)
    }

    @Test
    fun getMessageForSending_marksStatusSending() {
        every { mailMessageRepository.findOneWithTypeByIdAndStatus(mailMessage.id, possibleToSendMessageStatuses) }.returns(mailMessage)

        val actual = mailMessageService.getMessageForSending(mailMessage.id)

        assertEquals(mailMessage.id, actual.id)
        assertEquals(SENDING, actual.status)
    }

    @Test
    fun processSuccessfulDelivery_marksStatusSent() {
        every { mailMessageRepository.persist(capture(mailMessageSlot)) }.returns(Unit)

        mailMessageService.processSuccessfulDelivery(mailMessage)
        val actual = mailMessageSlot.captured

        assertEquals(mailMessage.id, actual.id)
        assertEquals(SENT, actual.status)
        assertNotNull(actual.sentAt)
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusRetry() {
        mailMessage.failedCount = 0

        every { mailMessageRepository.persist(capture(mailMessageSlot)) }.returns(Unit)

        mailMessageService.processFailedDelivery(mailMessage)
        val actual = mailMessageSlot.captured

        assertEquals(mailMessage.id, actual.id)
        assertEquals(RETRY, actual.status)
        assertEquals(1, actual.failedCount)
    }

    @Test
    fun processFailedDelivery_whenLimitIsNotExceeded_marksStatusFailed() {
        mailMessageType.maxRetriesCount = 10
        mailMessage.failedCount = 10

        every { mailMessageRepository.persist(capture(mailMessageSlot)) }.returns(Unit)

        mailMessageService.processFailedDelivery(mailMessage)
        val actual = mailMessageSlot.captured

        assertEquals(mailMessage.id, actual.id)
        assertEquals(FAILED, actual.status)
    }
}
