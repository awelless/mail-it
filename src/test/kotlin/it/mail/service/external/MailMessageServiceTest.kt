package it.mail.service.external

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageRepository
import it.mail.repository.MailMessageTypeRepository
import it.mail.service.BadRequestException
import it.mail.test.createMailMessageType
import it.mail.test.isUuid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @RelaxedMockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var mailMessageService: MailMessageService

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createMailMessageType()
    }

    @Nested
    inner class CreateNewMessage {

        @Test
        fun `when everything is correct - creates`() {
            // given
            val text = "Some message"
            val subject = "subject"
            val from = "from@gmail.com"
            val to = "to@mail.com"

            every { mailMessageTypeRepository.findOneByName(mailType.name) }.returns(mailType)
            every { mailMessageRepository.persist(capture(mailMessageSlot)) }.answers {}

            // when
            mailMessageService.createNewMail(text, subject, from, to, mailType.name)

            // then
            verify(exactly = 1) { mailMessageRepository.persist(any<MailMessage>()) }

            val savedMailMessage = mailMessageSlot.captured
            assertEquals(text, savedMailMessage.text)
            assertEquals(subject, savedMailMessage.subject)
            assertEquals(from, savedMailMessage.emailFrom)
            assertEquals(to, savedMailMessage.emailTo)
            assertTrue(savedMailMessage.externalId.isUuid())
            assertEquals(mailType, savedMailMessage.type)
        }

        @Test
        fun `when message type is invalid - throws exception`() {
            val mailTypeName = "invalid"
            every { mailMessageTypeRepository.findOneByName(mailTypeName) }.returns(null)

            assertThrows<BadRequestException> {
                mailMessageService.createNewMail("1", "sub", "em@gmail.com", "aa@gm.co", mailTypeName)
            }

            verify(exactly = 0) { mailMessageRepository.persist(any<MailMessage>()) }
        }
    }
}