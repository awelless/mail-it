package it.mail.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageRepository
import it.mail.service.external.ExternalMailMessageTypeService
import it.mail.service.external.MailMessageService
import it.mail.test.isUuid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository
    @RelaxedMockK
    lateinit var mailMessageTypeService: ExternalMailMessageTypeService

    @InjectMockKs
    lateinit var mailMessageService: MailMessageService

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = MailMessageType("DEFAULT")
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

            every { mailMessageTypeService.getTypeByName(mailType.name) }.returns(mailType)
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
    }
}