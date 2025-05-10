package io.mailit.core.service.mail

import io.mailit.core.api.connector.CreateMailRequest
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.idgenerator.test.ConstantIdGenerator
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState.PENDING
import io.mailit.value.exception.DuplicateUniqueKeyException
import io.mailit.value.exception.ValidationException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceImplTest {

    private val mailId = 1L

    @SpyK
    var idGenerator = ConstantIdGenerator(mailId)

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @RelaxedMockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var mailMessageService: MailMessageServiceImpl

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createPlainMailMessageType()
    }

    @Test
    fun `createNewMail - when everything is correct - creates`() = runTest {
        // given
        val command = CreateMailRequest(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com".toEmailAddress(),
            emailTo = "to@mail.com".toEmailAddress(),
            mailTypeName = mailType.name,
            deduplicationId = "deduplication",
        )

        coEvery { mailMessageTypeRepository.findByName(mailType.name) } returns mailType
        coEvery { mailMessageRepository.create(capture(mailMessageSlot)) } returns createMailMessage(mailType)

        // when
        mailMessageService.createNewMail(command)

        // then
        coVerify(exactly = 1) { mailMessageRepository.create(any()) }

        val savedMailMessage = mailMessageSlot.captured
        assertEquals(MailId(mailId), savedMailMessage.id)
        assertEquals(command.text, savedMailMessage.text)
        assertEquals(command.subject, savedMailMessage.subject)
        assertEquals(command.emailFrom, savedMailMessage.emailFrom)
        assertEquals(command.emailTo, savedMailMessage.emailTo)
        assertEquals(mailType, savedMailMessage.type)
        assertEquals(PENDING, savedMailMessage.state)
        assertEquals(command.deduplicationId, savedMailMessage.deduplicationId)
    }

    @Test
    fun `createNewMail - when mail is duplicate - does nothing`() = runTest {
        // given
        val command = CreateMailRequest(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com".toEmailAddress(),
            emailTo = "to@mail.com".toEmailAddress(),
            mailTypeName = mailType.name,
            deduplicationId = "deduplication",
        )

        coEvery { mailMessageTypeRepository.findByName(mailType.name) } returns mailType
        coEvery { mailMessageRepository.create(any()) } throws DuplicateUniqueKeyException(null, null)

        // when
        assertDoesNotThrow { mailMessageService.createNewMail(command) }

        // then
        coVerify(exactly = 1) { mailMessageRepository.create(any()) }
    }

    @Test
    fun `createNewMail - when message type is invalid - throws exception`() = runTest {
        val command = CreateMailRequest(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com".toEmailAddress(),
            emailTo = "to@mail.com".toEmailAddress(),
            mailTypeName = "invalid",
            deduplicationId = "deduplication",
        )

        coEvery { mailMessageTypeRepository.findByName(command.mailTypeName) } returns null

        assertThrows<ValidationException> { mailMessageService.createNewMail(command) }

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }
}
