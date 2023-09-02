package io.mailit.core.service.mail.type

import io.mailit.core.admin.api.type.CreateMailMessageTypeCommand
import io.mailit.core.admin.api.type.MailMessageContentType.HTML
import io.mailit.core.admin.api.type.MailMessageContentType.PLAIN_TEXT
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.PlainTextMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MailMessageTypeFactoryTest {

    private val plainTextMailTypeId = 1L
    private val htmlMailTypeId = 2L

    val mailMessageTypeFactory = MailMessageTypeFactoryManager(
        PlainTextMailMessageTypeFactory() { plainTextMailTypeId },
        HtmlMailMessageTypeFactory() { htmlMailTypeId },
    )

    @Nested
    inner class PlainContent {

        @Test
        fun create() {
            // given
            val command = CreateMailMessageTypeCommand(
                name = "name",
                description = "some description",
                maxRetriesCount = 100,
                contentType = PLAIN_TEXT,
                templateEngine = FREEMARKER,
                template = MailMessageTemplate("<html></html>"),
            )

            // when
            val mailType = mailMessageTypeFactory.create(command) as PlainTextMailMessageType

            // then
            assertEquals(plainTextMailTypeId, mailType.id)
            assertEquals(command.name, mailType.name)
            assertEquals(command.description, mailType.description)
            assertEquals(command.maxRetriesCount, mailType.maxRetriesCount)
        }
    }

    @Nested
    inner class HtmlContent {

        @Test
        fun validCommand_creates() {
            // given
            val command = CreateMailMessageTypeCommand(
                name = "name",
                description = "some description",
                maxRetriesCount = 100,
                contentType = HTML,
                templateEngine = FREEMARKER,
                template = MailMessageTemplate("<html></html>"),
            )

            // when
            val mailType = mailMessageTypeFactory.create(command) as HtmlMailMessageType

            // then
            assertEquals(htmlMailTypeId, mailType.id)
            assertEquals(command.name, mailType.name)
            assertEquals(command.description, mailType.description)
            assertEquals(command.maxRetriesCount, mailType.maxRetriesCount)
            assertEquals(command.templateEngine, mailType.templateEngine)
            assertEquals(command.template, mailType.template)
        }

        @Test
        fun noTemplateEngine_throwsException() {
            // given
            val command = CreateMailMessageTypeCommand(
                name = "name",
                description = "some description",
                maxRetriesCount = 100,
                contentType = HTML,
                templateEngine = null,
                template = MailMessageTemplate("<html></html>"),
            )

            // when
            val exception = assertThrows<ValidationException> { mailMessageTypeFactory.create(command) }

            // then
            assertEquals("No template engine is specified for html mail message type", exception.message)
        }

        @Test
        fun noTemplate_throwsException() {
            // given
            val command = CreateMailMessageTypeCommand(
                name = "name",
                description = "some description",
                maxRetriesCount = 100,
                contentType = HTML,
                templateEngine = FREEMARKER,
                template = null,
            )

            // when
            val exception = assertThrows<ValidationException> { mailMessageTypeFactory.create(command) }

            // then
            assertEquals("No template is specified for html mail message type", exception.message)
        }
    }
}
