package it.mail.service.external

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.ValidationException
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
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
class ExternalMailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @RelaxedMockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var mailMessageService: ExternalMailMessageService

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createPlainMailMessageType()
    }

    @Test
    fun `createNewMail - when everything is correct - creates`() = runTest {
        // given
        val text = "Some message"
        val subject = "subject"
        val from = "from@gmail.com"
        val to = "to@mail.com"

        coEvery { mailMessageTypeRepository.findById(mailType.id) }.returns(mailType)
        coEvery { mailMessageRepository.create(capture(mailMessageSlot)) }.returns(createMailMessage(mailType))

        // when
        mailMessageService.createNewMail(text, subject, from, to, mailType.id)

        // then
        coVerify(exactly = 1) { mailMessageRepository.create(any()) }

        val savedMailMessage = mailMessageSlot.captured
        assertEquals(text, savedMailMessage.text)
        assertEquals(subject, savedMailMessage.subject)
        assertEquals(from, savedMailMessage.emailFrom)
        assertEquals(to, savedMailMessage.emailTo)
        assertEquals(mailType, savedMailMessage.type)
        assertEquals(PENDING, savedMailMessage.status)
    }

    @Test
    fun `createNewMail - when message type is invalid - throws exception`() = runTest {
        val mailTypeId = 12L
        coEvery { mailMessageTypeRepository.findById(mailTypeId) }.returns(null)

        assertThrows<ValidationException> {
            mailMessageService.createNewMail("1", "sub", "em@gmail.com", "aa@gm.co", mailTypeId)
        }

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    @ParameterizedTest
    @MethodSource("invalidDataForCreation")
    fun `createNewMail - with invalid data - throws exception`(text: String, subject: String?, emailFrom: String?, emailTo: String, expectedMessage: String) = runTest {
        val exception = assertThrows<ValidationException> {
            mailMessageService.createNewMail(text, subject, emailFrom, emailTo, 1)
        }

        assertEquals(expectedMessage, exception.message)

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    companion object {
        @JvmStatic
        private fun invalidDataForCreation(): List<Arguments> = listOf(
            arguments("", "subject", "to-email@gmail.com", "email@gmail.com", "text shouldn't be empty"),
            arguments("text", "subject", "email.email.com", "email@gmail.com", "emailFrom is incorrect"),
            arguments("text", "subject", "email@email.com", "", "emailTo shouldn't be blank"),
            arguments("text", "subject", null, "email.email.com", "emailTo is incorrect"),
        )
    }
}
