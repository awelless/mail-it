package it.mail.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import it.mail.domain.MailMessage
import it.mail.repository.MailMessageRepository
import it.mail.test.isUuid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageServiceTest {

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @InjectMockKs
    lateinit var mailMessageService: MailMessageService

    val mailMessageSlot = slot<MailMessage>()

    @Nested
    inner class CreateNewMessage {

        @Test
        fun `when everything is correct - creates`() {
            // given
            val text = "Some message"
            val subject = "subject"
            val from = "from@gmail.com"
            val to = "to@mail.com"

            every { mailMessageRepository.persist(capture(mailMessageSlot)) }.answers {}

            // when
            mailMessageService.createNewMessage(text, subject, from, to)

            // then
            verify(exactly = 1) { mailMessageRepository.persist(any<MailMessage>()) }

            val savedMailMessage = mailMessageSlot.captured
            assertEquals(text, savedMailMessage.text)
            assertEquals(subject, savedMailMessage.subject)
            assertEquals(from, savedMailMessage.emailFrom)
            assertEquals(to, savedMailMessage.emailTo)
            assertTrue(savedMailMessage.externalId.isUuid())
        }
    }
}