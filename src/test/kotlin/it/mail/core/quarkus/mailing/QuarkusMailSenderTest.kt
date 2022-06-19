package it.mail.core.quarkus.mailing

import io.quarkus.mailer.Mail
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.junit.QuarkusTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class QuarkusMailSenderTest {

    @Inject
    lateinit var mockMailbox: MockMailbox

    @Inject
    lateinit var mailSender: QuarkusMailSender

    @AfterEach
    fun tearDown() {
        mockMailbox.clear()
    }

    @Test
    fun send_sends() = runTest {
        // given
        val mail = Mail.withText("email@to.com", "subject", "some text")

        // when
        mailSender.send(mail)
        val sentMails = mockMailbox.getMessagesSentTo(mail.to[0])
        val sentMail = sentMails[0]

        // then
        assertEquals(1, sentMails.size)

        assertEquals(mail.text, sentMail.text)
        assertEquals(mail.subject, sentMail.subject)
        assertEquals(mail.to, sentMail.to)
        assertEquals(mail.from, sentMail.from)
    }
}
