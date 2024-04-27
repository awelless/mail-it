package io.mailit.persistence.test

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.test.nowWithoutNanos
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.WritePersistenceMail
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class WorkerMailRepositoryTest {

    @Inject
    lateinit var mailRepository: MailRepository

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

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
                state = MailState.PENDING,
                failedCount = 0,
                deduplicationId = "deduplication1",
            )
            mailRepository.create(mailMessage)
        }
    }

    @Test
    fun create() = runTest {
        // given
        val message = WritePersistenceMail(
            id = MailId(555),
            mailTypeId = mailMessageType.id,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = nowWithoutNanos(),
            sendingStartedAt = null,
            sentAt = null,
            state = MailState.PENDING,
            failedCount = 0,
            deduplicationId = "deduplication555",
        )

        // when
        mailRepository.create(message).getOrThrow()

        val actual = mailMessageRepository.findOneWithTypeById(message.id)!!

        // then
        assertEquals(message.id, actual.id)
        assertEquals(message.mailTypeId, actual.type.id)
        assertEquals(message.text, actual.text)
        assertEquals(message.data, actual.data)
        assertEquals(message.subject, actual.subject)
        assertEquals(message.emailFrom, actual.emailFrom)
        assertEquals(message.emailTo, actual.emailTo)
        assertEquals(message.createdAt, actual.createdAt)
        assertEquals(message.sendingStartedAt, actual.sendingStartedAt)
        assertEquals(message.sentAt, actual.sentAt)
        assertEquals(message.state, actual.state)
        assertEquals(message.failedCount, actual.failedCount)
        assertEquals(message.deduplicationId, actual.deduplicationId)
    }

    @Test
    fun `create - with the same deduplication id - return error`() = runTest {
        // given
        val message = WritePersistenceMail(
            id = MailId(123),
            mailTypeId = mailMessageType.id,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = nowWithoutNanos(),
            sendingStartedAt = null,
            sentAt = null,
            state = MailState.PENDING,
            failedCount = 0,
            deduplicationId = mailMessage.deduplicationId,
        )

        // when
        val result = mailRepository.create(message)

        // then
        assertTrue(result.exceptionOrNull() is DuplicateUniqueKeyException)
    }

    @Test
    fun `create - without deduplication ids - create 2`() = runTest {
        // given
        val message = WritePersistenceMail(
            id = MailId(123),
            mailTypeId = mailMessageType.id,
            text = null,
            data = mapOf("name" to "Name", "age" to 20),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = nowWithoutNanos(),
            sendingStartedAt = null,
            sentAt = null,
            state = MailState.PENDING,
            failedCount = 0,
            deduplicationId = null,
        )

        val message2 = message.copy(id = MailId(124))

        // when
        mailRepository.create(message).getOrThrow()
        mailRepository.create(message2).getOrThrow()

        // then
        assertNotNull(mailMessageRepository.findOneWithTypeById(message.id))
        assertNotNull(mailMessageRepository.findOneWithTypeById(message2.id))
    }
}
