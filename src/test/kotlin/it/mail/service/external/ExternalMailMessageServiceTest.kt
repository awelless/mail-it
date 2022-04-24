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
import it.mail.service.BadRequestException
import it.mail.test.createMailMessage
import it.mail.test.createMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

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
        mailType = createMailMessageType()
    }

    @Nested
    inner class CreateNewMessage {

        @Test
        suspend fun `when everything is correct - creates`() {
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
        suspend fun `when message type is invalid - throws exception`() {
            val mailTypeId = 12L
            coEvery { mailMessageTypeRepository.findById(mailTypeId) }.returns(null)

            assertThrows<BadRequestException> {
                mailMessageService.createNewMail("1", "sub", "em@gmail.com", "aa@gm.co", mailTypeId)
            }

            coVerify(exactly = 0) { mailMessageRepository.create(any()) }
        }
    }
}
