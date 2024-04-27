package io.mailit.persistence.test

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
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.WritePersistenceMail
import jakarta.inject.Inject
import java.time.Instant
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class MailMessageRepositoryTest {

    @Inject
    lateinit var mailRepository: MailRepository

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository

    lateinit var mailMessage: WritePersistenceMail
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)

            mailMessage = WritePersistenceMail(
                id = MailId(1),
                mailTypeId = mailMessageType.id,
                text = "text",
                data = emptyMap(),
                subject = null,
                emailFrom = "email@from.com".toEmailAddress(),
                emailTo = "email@to.com".toEmailAddress(),
                createdAt = nowWithoutNanos(),
                sendingStartedAt = null,
                sentAt = null,
                state = PENDING,
                failedCount = 0,
                deduplicationId = "deduplication1",
            )
            mailRepository.create(mailMessage)
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

        val sendingMessage = WritePersistenceMail(
            id = MailId(2),
            mailTypeId = mailMessageType.id,
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = Instant.now().minusSeconds(100),
            sendingStartedAt = messageSendingStartedAt,
            sentAt = null,
            state = SENDING,
            failedCount = 0,
            deduplicationId = "deduplication2",
        )
        mailRepository.create(sendingMessage)

        // when
        val actual = mailMessageRepository.findAllWithTypeByStatesAndSendingStartedBefore(listOf(SENDING), Instant.now(), 1000)

        // then
        assertEquals(1, actual.size)
        assertEquals(sendingMessage.id, actual.first().id)
    }

    @Test
    fun findAllIdsByStateIn_returnsIdsOnly() = runTest {
        // given
        val message2 = WritePersistenceMail(
            id = MailId(2),
            mailTypeId = mailMessageType.id,
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = Instant.now(),
            sendingStartedAt = null,
            sentAt = null,
            state = PENDING,
            failedCount = 0,
            deduplicationId = "deduplication2",
        )
        mailRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllIdsByStateIn(listOf(PENDING), 1000)

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }

    @Test
    fun findAllSlicedDescendingIdSorted() = runTest {
        // given
        val message2 = WritePersistenceMail(
            id = MailId(2),
            mailTypeId = mailMessageType.id,
            text = "text",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = Instant.now(),
            sendingStartedAt = null,
            sentAt = null,
            state = PENDING,
            failedCount = 0,
            deduplicationId = "deduplication2",
        )
        mailRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllSlicedDescendingIdSorted(page = 1, size = 1)

        // then
        assertEquals(1, actual.size)
        assertEquals(mailMessage.id, actual.content[0].id)
        assertTrue(actual.last)
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
