package io.mailit.core.service.quarkus.mailing

import io.mailit.template.test.StubTemplateProcessor
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MailFactoryTest {

    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }

    private val templateProcessor = StubTemplateProcessor("some html")
    private val mailFactory = MailFactory(templateProcessor)

    @Test
    fun create_plainTextMessage() = runTest {
        // given
        val plainMessageType = createPlainMailMessageType()
        val message = createMailMessage(plainMessageType)

        // when
        val actual = mailFactory.create(message)

        // then
        assertEquals(message.text, actual.text)
        assertEquals(message.subject, actual.subject)
        assertEquals(listOf(message.emailTo), actual.to)
        assertEquals(message.emailFrom, actual.from)
        assertEquals(listOf("${message.id}@mail-it.io"), actual.headers[MESSAGE_ID_HEADER])
    }

    @Test
    fun create_htmlMessage() = runTest {
        // given
        val htmlMessageType = createHtmlMailMessageType()
        val message = createMailMessage(htmlMessageType)

        // when
        val actual = mailFactory.create(message)

        // then
        assertEquals(templateProcessor.html, actual.html)
        assertEquals(message.subject, actual.subject)
        assertEquals(listOf(message.emailTo), actual.to)
        assertEquals(message.emailFrom, actual.from)
        assertEquals(listOf("${message.id}@mail-it.io"), actual.headers[MESSAGE_ID_HEADER])
    }
}
