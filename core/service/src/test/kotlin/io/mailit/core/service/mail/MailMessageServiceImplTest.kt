package io.mailit.core.service.mail

import io.mailit.core.exception.ValidationException
import io.mailit.core.external.api.CreateMailCommand
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus.PENDING
import io.mailit.core.model.MailMessageType
import io.mailit.core.service.id.IdGenerator
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class MailMessageServiceImplTest {

    private val mailId = 1L

    @SpyK
    var idGenerator = object : IdGenerator { // spyk doesn't work well with lambdas
        override fun generateId() = mailId
    }

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
        val command = CreateMailCommand(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com",
            emailTo = "to@mail.com",
            mailType = mailType.name,
        )

        coEvery { mailMessageTypeRepository.findByName(mailType.name) } returns mailType
        coEvery { mailMessageRepository.create(capture(mailMessageSlot)) } returns createMailMessage(mailType)

        // when
        mailMessageService.createNewMail(command)

        // then
        coVerify(exactly = 1) { mailMessageRepository.create(any()) }

        val savedMailMessage = mailMessageSlot.captured
        assertEquals(mailId, savedMailMessage.id)
        assertEquals(command.text, savedMailMessage.text)
        assertEquals(command.subject, savedMailMessage.subject)
        assertEquals(command.emailFrom, savedMailMessage.emailFrom)
        assertEquals(command.emailTo, savedMailMessage.emailTo)
        assertEquals(mailType, savedMailMessage.type)
        assertEquals(PENDING, savedMailMessage.status)
    }

    @Test
    fun `createNewMail - when message type is invalid - throws exception`() = runTest {
        val command = CreateMailCommand(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com",
            emailTo = "to@mail.com",
            mailType = "invalid",
        )

        coEvery { mailMessageTypeRepository.findByName(command.mailType) } returns null

        assertThrows<ValidationException> { mailMessageService.createNewMail(command) }

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    @ParameterizedTest
    @MethodSource("invalidDataForCreation")
    fun `createNewMail - with invalid data - throws exception`(
        subject: String?,
        emailFrom: String?,
        emailTo: String,
        expectedMessage: String,
    ) = runTest {
        val command = CreateMailCommand(
            text = "123",
            data = emptyMap(),
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            mailType = "123",
        )

        val exception = assertThrows<ValidationException> {
            mailMessageService.createNewMail(command)
        }

        assertEquals(expectedMessage, exception.message)

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    companion object {
        @JvmStatic
        private fun invalidDataForCreation(): List<Arguments> = listOf(
            arguments("subject", "email.email.com", "email@gmail.com", "emailFrom is incorrect"),
            arguments("subject", "email@email.com", "", "emailTo shouldn't be blank"),
            arguments("subject", null, "email.email.com", "emailTo is incorrect"),
        )
    }
}
