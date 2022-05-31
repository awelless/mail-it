package it.mail.core.admin

import io.mockk.junit5.MockKExtension
import it.mail.core.ValidationException
import it.mail.core.admin.MailMessageContentType.HTML
import it.mail.core.admin.MailMessageContentType.PLAIN_TEXT
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine.FREEMARKER
import it.mail.core.model.PlainTextMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageTypeFactoryTest {

    val mailMessageTypeFactory = MailMessageTypeFactoryManager(
        PlainTextMailMessageTypeFactory(),
        HtmlMailMessageTypeFactory(),
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
                template = "<html></html>",
            )

            // when
            val mailType = mailMessageTypeFactory.create(command) as PlainTextMailMessageType

            // then
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
                template = "<html></html>",
            )

            // when
            val mailType = mailMessageTypeFactory.create(command) as HtmlMailMessageType

            // then
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
                template = "<html></html>",
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
