package io.mailit.core.service.quarkus.mailing

import io.mailit.core.service.mailing.templates.TemplateProcessor
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.createMailMessage
import io.mailit.test.createPlainMailMessageType
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailFactoryTest {

    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }

    @MockK
    lateinit var templateProcessor: TemplateProcessor

    @InjectMockKs
    lateinit var mailFactory: MailFactory

    @Test
    fun create_plainTextMessage() {
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
    fun create_htmlMessage() {
        // given
        val htmlMessageType = createHtmlMailMessageType()
        val message = createMailMessage(htmlMessageType)

        val messageHtml = "<html><body> message </body></html>"

        every { templateProcessor.process(htmlMessageType, message.data.orEmpty()) } returns messageHtml

        // when
        val actual = mailFactory.create(message)

        // then
        assertEquals(messageHtml, actual.html)
        assertEquals(message.subject, actual.subject)
        assertEquals(listOf(message.emailTo), actual.to)
        assertEquals(message.emailFrom, actual.from)
        assertEquals(listOf("${message.id}@mail-it.io"), actual.headers[MESSAGE_ID_HEADER])
    }
}
