package io.mailit.worker

import io.mailit.core.exception.ValidationException
import io.mailit.idgenerator.test.ConstantIdGenerator
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.value.MailTypeId
import io.mailit.worker.api.CreateMail
import io.mailit.worker.api.CreateMailRequest
import io.mailit.worker.context.WorkerContext
import io.mailit.worker.test.InMemoryMailRepository
import io.mailit.worker.test.InMemoryMailTypeRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WorkerTest {

    private val now = Instant.now()
    private val constantId = 1L

    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val idGenerator = ConstantIdGenerator(constantId)
    private lateinit var mailRepository: InMemoryMailRepository
    private lateinit var mailTypeRepository: InMemoryMailTypeRepository

    private lateinit var context: WorkerContext

    @BeforeEach
    fun setUp() {
        mailRepository = InMemoryMailRepository()
        mailTypeRepository = InMemoryMailTypeRepository()

        context = WorkerContext.create(clock, idGenerator, mailRepository, mailTypeRepository)
    }

    @Nested
    inner class CreateMailTest {

        private val mailTypeId = MailTypeId(333)
        private val mailTypeName = "name"

        private lateinit var createMail: CreateMail

        @BeforeEach
        fun setUp() {
            createMail = context.createMail

            mailTypeRepository.addType(mailTypeId, mailTypeName)
        }

        @Test
        fun `create - when everything is correct - creates`() = runTest {
            // given
            val request = CreateMailRequest(
                text = "Some message",
                data = mapOf("name" to "john"),
                subject = "subject",
                emailFrom = "from@gmail.com".toEmailAddress(),
                emailTo = "to@mail.com".toEmailAddress(),
                mailTypeName = mailTypeName,
                deduplicationId = "deduplication",
            )

            // when
            val result = createMail(request)

            val created = mailRepository.getCreatedMails()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, created.size)

            val createdMail = created.first()

            assertEquals(MailId(constantId), createdMail.id)
            assertEquals(mailTypeId, createdMail.mailTypeId)

            assertEquals(request.text, createdMail.text)
            assertEquals(request.data, createdMail.data)
            assertEquals(request.subject, createdMail.subject)
            assertEquals(request.emailFrom, createdMail.emailFrom)
            assertEquals(request.emailTo, createdMail.emailTo)

            assertEquals(now, createdMail.createdAt)
            assertNull(createdMail.sendingStartedAt)
            assertNull(createdMail.sentAt)

            assertEquals(MailState.PENDING, createdMail.state)
            assertEquals(0, createdMail.failedCount)

            assertEquals(request.deduplicationId, createdMail.deduplicationId)
        }

        @Test
        fun `createNewMail - when mail is duplicate - does nothing`() = runTest {
            // given
            val request = CreateMailRequest(
                text = "Some message",
                data = mapOf("name" to "john"),
                subject = "subject",
                emailFrom = "from@gmail.com".toEmailAddress(),
                emailTo = "to@mail.com".toEmailAddress(),
                mailTypeName = mailTypeName,
                deduplicationId = "deduplication",
            )

            // when
            val firstResult = createMail(request)
            val secondResult = createMail(request)

            val created = mailRepository.getCreatedMails()

            // then
            assertTrue(firstResult.isSuccess)
            assertTrue(secondResult.isSuccess)

            assertEquals(1, created.size)
        }

        @Test
        fun `createNewMail - when message type is invalid - returns error`() = runTest {
            // given
            val request = CreateMailRequest(
                text = "Some message",
                data = mapOf("name" to "john"),
                subject = "subject",
                emailFrom = "from@gmail.com".toEmailAddress(),
                emailTo = "to@mail.com".toEmailAddress(),
                mailTypeName = "invalid",
                deduplicationId = "deduplication",
            )

            // when
            val result = createMail(request)

            // then
            assertEquals(ValidationException("Invalid type: ${request.mailTypeName} is passed"), result.exceptionOrNull())

            assertTrue(mailRepository.getCreatedMails().isEmpty())
        }
    }
}
