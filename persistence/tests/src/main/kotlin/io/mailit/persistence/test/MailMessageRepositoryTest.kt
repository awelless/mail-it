package io.mailit.persistence.test

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.test.nowWithoutNanos
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState.CANCELED
import io.mailit.value.MailState.PENDING
import io.mailit.value.MailState.SENDING
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
                id = MailId(1),
                text = "text",
                data = emptyMap(),
                subject = null,
                emailFrom = "email@from.com".toEmailAddress(),
                emailTo = "email@to.com".toEmailAddress(),
                type = mailMessageType,
                createdAt = nowWithoutNanos(),
                state = PENDING,
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
        assertEquals(mailMessage.state, actual.state)
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
    fun findAllWithTypeByStatesAndSendingStartedBefore_returns() = runTest {
        // given
        val messageSendingStartedAt = Instant.now().minusSeconds(10)

        val sendingMessage = MailMessage(
            id = MailId(2),
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = Instant.now().minusSeconds(100),
            sendingStartedAt = messageSendingStartedAt,
            state = SENDING,
            deduplicationId = "deduplication2",
        )
        mailMessageRepository.create(sendingMessage)

        // when
        val actual = mailMessageRepository.findAllWithTypeByStatesAndSendingStartedBefore(listOf(SENDING), Instant.now(), 1000)

        // then
        assertEquals(1, actual.size)
        assertEquals(sendingMessage.id, actual.first().id)
    }

    @Test
    fun findAllIdsByStateIn_returnsIdsOnly() = runTest {
        // given
        val message2 = MailMessage(
            id = MailId(2),
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = Instant.now(),
            state = PENDING,
            deduplicationId = "deduplication2",
        )
        mailMessageRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllIdsByStateIn(listOf(PENDING), 1000)

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }

    @Test
    fun findAllSlicedDescendingIdSorted() = runTest {
        // given
        val message2 = MailMessage(
            id = MailId(2),
            text = "text",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = Instant.now(),
            state = PENDING,
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
            id = MailId(555),
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            state = PENDING,
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
            id = MailId(123),
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            state = PENDING,
            deduplicationId = mailMessage.deduplicationId,
        )

        // when + then
        assertThrows<DuplicateUniqueKeyException> { mailMessageRepository.create(message) }
    }

    @Test
    fun `create without deduplication ids`() = runTest {
        // given
        val message = MailMessage(
            id = MailId(123),
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            type = mailMessageType,
            createdAt = nowWithoutNanos(),
            state = PENDING,
            deduplicationId = null,
        )

        val message2 = message.copy(id = MailId(124))

        // when
        mailMessageRepository.create(message)
        mailMessageRepository.create(message2)

        // then
        assertNotNull(mailMessageRepository.findOneWithTypeById(message.id))
        assertNotNull(mailMessageRepository.findOneWithTypeById(message2.id))
    }

    @Test
    fun updateMessageState() = runTest {
        // given
        val state = SENDING

        // when
        mailMessageRepository.updateMessageState(mailMessage.id, state)

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(state, actual?.state)
    }

    @Test
    fun updateMessageStateAndSendingStartedTimeByIdAndStateIn_whenStatesMatch_updates() = runTest {
        // given
        val state = SENDING
        val startTime = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStateAndSendingStartedTimeByIdAndStateIn(
            id = mailMessage.id,
            states = listOf(mailMessage.state),
            state = state,
            sendingStartedAt = startTime,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(state, actual?.state)
        assertEquals(startTime, actual?.sendingStartedAt)
    }

    @Test
    fun updateMessageStateAndSendingStartedTimeByIdAndStateIn_whenStatesDoNotMatch_doesNothing() = runTest {
        // given
        val state = SENDING
        val startTime = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStateAndSendingStartedTimeByIdAndStateIn(
            id = mailMessage.id,
            states = listOf(CANCELED),
            state = state,
            sendingStartedAt = startTime,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(mailMessage.state, actual?.state)
        assertEquals(mailMessage.sendingStartedAt, actual?.sendingStartedAt)
    }

    @Test
    fun updateMessageStateAndSentTime() = runTest {
        // given
        val state = SENDING
        val sentAt = nowWithoutNanos()

        // when
        mailMessageRepository.updateMessageStateAndSentTime(
            id = mailMessage.id,
            state = state,
            sentAt = sentAt,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(state, actual?.state)
        assertEquals(sentAt, actual?.sentAt)
    }

    @Test
    fun updateMessageStateFailedCountAndSendingStartedTime() = runTest {
        // given
        val state = SENDING
        val failedCount = 123

        // when
        mailMessageRepository.updateMessageStateFailedCountAndSendingStartedTime(
            id = mailMessage.id,
            state = state,
            failedCount = failedCount,
            sendingStartedAt = null,
        )

        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)

        // then
        assertEquals(state, actual?.state)
        assertNull(actual?.sendingStartedAt)
    }
}
