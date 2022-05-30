package it.mail.service.mailing

import io.quarkus.mailer.MockMailbox
import io.quarkus.test.junit.QuarkusTest
import it.mail.domain.MailMessage
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class MailSenderTest {

    @Inject
    lateinit var mockMailbox: MockMailbox

    @Inject
    lateinit var mailSender: MailSender

    lateinit var mailMessage: MailMessage

    @BeforeEach
    fun setUp() {
        val mailType = createPlainMailMessageType()
        mailMessage = createMailMessage(mailType)
    }

    @AfterEach
    fun tearDown() {
        mockMailbox.clear()
    }

    @Test
    fun send_sends() = runTest {
        // when
        mailSender.send(mailMessage)
        val sentMails = mockMailbox.getMessagesSentTo(mailMessage.emailTo)
        val sentMail = sentMails[0]

        // then
        assertEquals(1, sentMails.size)

        assertEquals(mailMessage.text, sentMail.text)
        assertEquals(mailMessage.subject, sentMail.subject)
        assertEquals(listOf(mailMessage.emailTo), sentMail.to)
        assertEquals(mailMessage.emailFrom, sentMail.from)
    }
}
