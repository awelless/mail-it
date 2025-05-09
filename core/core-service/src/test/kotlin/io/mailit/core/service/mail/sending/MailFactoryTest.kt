package io.mailit.core.service.mail.sending

import io.mailit.core.spi.mailer.MailContent
import io.mailit.template.test.StubTemplateProcessor
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MailFactoryTest {

    private val templateProcessor = StubTemplateProcessor("some html")
    private val mailFactory = MailFactory(templateProcessor)

    @Test
    fun create_plainTextMessage() = runTest {
        // given
        val plainMessageType = createPlainMailMessageType()
        val message = createMailMessage(plainMessageType)

        // when
        val actual = mailFactory.create(message).getOrThrow()

        // then
        assertEquals(MailContent.Text(message.text.orEmpty()), actual.content)
        assertEquals(message.subject, actual.subject)
        assertEquals(message.emailTo, actual.emailTo)
        assertEquals(message.emailFrom, actual.emailFrom)
        assertEquals(listOf("${message.id}@mail-it.io"), actual.headers[MESSAGE_ID_HEADER])
    }

    @Test
    fun create_htmlMessage() = runTest {
        // given
        val htmlMessageType = createHtmlMailMessageType()
        val message = createMailMessage(htmlMessageType)

        // when
        val actual = mailFactory.create(message).getOrThrow()

        // then
        assertEquals(MailContent.Html(templateProcessor.html), actual.content)
        assertEquals(message.subject, actual.subject)
        assertEquals(message.emailTo, actual.emailTo)
        assertEquals(message.emailFrom, actual.emailFrom)
        assertEquals(listOf("${message.id}@mail-it.io"), actual.headers[MESSAGE_ID_HEADER])
    }

    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }
}
