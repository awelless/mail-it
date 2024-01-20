package io.mailit.persistence.test

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus.CANCELED
import io.mailit.core.model.MailMessageStatus.PENDING
import io.mailit.core.model.MailMessageStatus.SENDING
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.test.nowWithoutNanos
import jakarta.inject.Inject
import java.time.Instant
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

abstract class MailMessageRepositoryTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository

    lateinit var mailMessage: MailMessage
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)

            mailMessage = MailMessage(
                id = 1,
                text = "text",
                data = emptyMap(),
                subject = null,
                emailFrom = "email@from.com",
                emailTo = "email@to.com",
                type = mailMessageType,
                createdAt = nowWithoutNanos(),
                status = PENDING,
                deduplicationId = "deduplication1",
            )
            mailMessageRepository.create(mailMessage)
        }
    }

    @Test
    fun findOneWithTypeById_fetchesMailType() = runTest {
        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)!!

        assertEquals(mailMessage.id, actual.id)
        assertEquals(mailMessage.subject, actual.subject)
        assertEquals(mailMessage.emailFrom, actual.emailFrom)
        assertEquals(mailMessage.emailTo, actual.emailTo)
        assertEquals(mailMessage.createdAt, actual.createdAt)
        assertEquals(mailMessage.status, actual.status)
        assertEquals(mailMessage.deduplicationId, actual.deduplicationId)

        // mailMessageType is fetched too
        assertEquals(mailMessageType.id, actual.type.id)
        assertEquals(mailMessageType.name, actual.type.name)
        assertEquals(mailMessageType.description, actual.type.description)
        assertEquals(mailMessageType.maxRetriesCount, actual.type.maxRetriesCount)
        assertEquals(mailMessageType.createdAt, actual.type.createdAt)
        assertEquals(mailMessageType.updatedAt, actual.type.updatedAt)
    }

    @Test
    fun findAllWithTypeByStatusesAndSendingStartedBefore_returns() = runTest {
        // given
        val messageSendingStartedAt = Instant.now().minusSeconds(10)

        val sendingMessage = MailMessage(
            id = 2,
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = Instant.now().minusSeconds(100),
            sendingStartedAt = messageSendingStartedAt,
            status = SENDING,
            deduplicationId = "deduplication2",
        )
        mailMessageRepository.create(sendingMessage)

        // when
        val actual = mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(listOf(SENDING), Instant.now(), 1000)

        // then
        assertEquals(1, actual.size)
        assertEquals(sendingMessage.id, actual.first().id)
    }

    @Test
    fun findAllIdsByStatusIn_returnsIdsOnly() = runTest {
        // given
        val message2 = MailMessage(
            id = 2,
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = Instant.now(),
            status = PENDING,
            deduplicationId = "deduplication2",
        )
        mailMessageRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllIdsByStatusIn(listOf(PENDING), 1000)

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }

    @Test
    fun findAllSlicedDescendingIdSorted() = runTest {
        // given
        val message2 = MailMessage(
            id = 2,
            text = "text",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = Instant.now(),
            status = PENDING,
            deduplicationId = "deduplication2",
        )
        mailMessageRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllSlicedDescendingIdSorted(page = 1, size = 1)

        // then
        assertEquals(listOf(mailMessage), actual.content)
        assertTrue(actual.last)
    }

    @Test
    fun create() = runTest {
        // given
        val message = MailMessage(
            id = 555,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            status = PENDING,
            deduplicationId = "deduplication555",
        )

        // when
        mailMessageRepository.create(message)

        val actual = mailMessageRepository.findOneWithTypeById(message.id)

        // then
        assertEquals(message, actual)
    }

    @Test
    fun `create with the same deduplication id`() = runTest {
        // given
        val message = MailMessage(
            id = 123,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            status = PENDING,
            deduplicationId = mailMessage.deduplicationId,
        )

        // when + then
        assertThrows<DuplicateUniqueKeyException> { mailMessageRepository.create(message) }
    }

    @Test
    fun `create without deduplication ids`() = runTest {
        // given
        val message = MailMessage(
            id = 123,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            status = PENDING,
            deduplicationId = null,
        )

        val message2 = message.copy(id = 124)

        // when
        mailMessageRepository.create(message)
        mailMessageRepository.create(message2)

        // then
        assertNotNull(mailMessageRepository.findOneWithTypeById(message.id))
        assertNotNull(mailMessageRepository.findOneWithTypeById(message2.id))
    }

    @Test
    fun updateMessageStatus() = runTest {
        // given
        val status = SENDING

        // when
        mailMessageRepository.updateMessageStatus(mailMessage.id, status)

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(status, actual?.status)
    }

    @Test
    fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn_whenStatusesMatch_updates() = runTest {
        // given
        val status = SENDING
        val startTime = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
            id = mailMessage.id,
            statuses = listOf(mailMessage.status),
            status = status,
            sendingStartedAt = startTime,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(status, actual?.status)
        assertEquals(startTime, actual?.sendingStartedAt)
    }

    @Test
    fun updateMessageStatusAndSendingStartedTimeByIdAndStatusIn_whenStatusesDoNotMatch_doesNothing() = runTest {
        // given
        val status = SENDING
        val startTime = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(
            id = mailMessage.id,
            statuses = listOf(CANCELED),
            status = status,
            sendingStartedAt = startTime,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(mailMessage.status, actual?.status)
        assertEquals(mailMessage.sendingStartedAt, actual?.sendingStartedAt)
    }

    @Test
    fun updateMessageStatusAndSentTime() = runTest {
        // given
        val status = SENDING
        val sentAt = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStatusAndSentTime(
            id = mailMessage.id,
            status = status,
            sentAt = sentAt,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(status, actual?.status)
        assertEquals(sentAt, actual?.sentAt)
    }

    @Test
    fun updateMessageStatusFailedCountAndSendingStartedTime() = runTest {
        // given
        val status = SENDING
        val failedCount = 123

        // when
        mailMessageRepository.updateMessageStatusFailedCountAndSendingStartedTime(
            id = mailMessage.id,
            status = status,
            failedCount = failedCount,
            sendingStartedAt = null,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(status, actual?.status)
        assertNull(actual?.sendingStartedAt)
    }
}
