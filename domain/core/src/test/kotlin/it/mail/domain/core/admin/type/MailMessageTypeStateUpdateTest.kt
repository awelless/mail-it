package it.mail.domain.core.admin.type

import io.mockk.junit5.MockKExtension
import it.mail.domain.admin.api.type.UpdateMailMessageTypeCommand
import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.HtmlTemplateEngine.FREEMARKER
import it.mail.domain.model.HtmlTemplateEngine.NONE
import it.mail.domain.model.PlainTextMailMessageType
import it.mail.exception.ValidationException
import it.mail.test.createHtmlMailMessageType
import it.mail.test.createPlainMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageTypeStateUpdateTest {

    val mailMessageTypeFactory = MailMessageTypeStateUpdaterManager(
        PlainTextMailMessageTypeStateUpdater(),
        HtmlMailMessageTypeStateUpdater(),
    )

    @Nested
    inner class PlainContent {

        lateinit var mailType: PlainTextMailMessageType

        @BeforeEach
        fun setUp() {
            mailType = createPlainMailMessageType()
        }

        @Test
        fun update() {
            // given
            val command = UpdateMailMessageTypeCommand(
                id = mailType.id,
                description = "some description",
                maxRetriesCount = 100,
            )

            // when
            mailMessageTypeFactory.update(mailType, command)

            // then
            assertEquals(command.description, mailType.description)
            assertEquals(command.maxRetriesCount, mailType.maxRetriesCount)
        }
    }

    @Nested
    inner class HtmlContent {

        lateinit var mailType: HtmlMailMessageType

        @BeforeEach
        fun setUp() {
            mailType = createHtmlMailMessageType()
        }

        @Test
        fun validCommand_updates() {
            // given
            val command = UpdateMailMessageTypeCommand(
                id = mailType.id,
                description = "some description",
                maxRetriesCount = 100,
                templateEngine = FREEMARKER,
                template = "<html></html>",
            )

            // when
            mailMessageTypeFactory.update(mailType, command)

            // then
            assertEquals(command.description, mailType.description)
            assertEquals(command.maxRetriesCount, mailType.maxRetriesCount)
            assertEquals(command.templateEngine, mailType.templateEngine)
            assertEquals(command.template, mailType.template)
        }

        @Test
        fun noTemplateEngine_throwsException() {
            // given
            val command = UpdateMailMessageTypeCommand(
                id = mailType.id,
                description = "some description",
                maxRetriesCount = 100,
                templateEngine = null,
                template = "<html></html>",
            )

            // when
            val exception = assertThrows<ValidationException> { mailMessageTypeFactory.update(mailType, command) }

            // then
            assertEquals("No template engine is specified for html mail message type", exception.message)
        }

        @Test
        fun noTemplate_throwsException() {
            // given
            val command = UpdateMailMessageTypeCommand(
                id = mailType.id,
                description = "some description",
                maxRetriesCount = 100,
                templateEngine = NONE,
                template = null,
            )

            // when
            val exception = assertThrows<ValidationException> { mailMessageTypeFactory.update(mailType, command) }

            // then
            assertEquals("No template is specified for html mail message type", exception.message)
        }
    }
}
