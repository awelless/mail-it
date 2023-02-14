package io.mailit.core.service.admin.type

import io.mailit.core.admin.api.type.UpdateMailMessageTypeCommand
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.core.model.HtmlTemplateEngine.NONE
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.createPlainMailMessageType
import io.mockk.junit5.MockKExtension
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
