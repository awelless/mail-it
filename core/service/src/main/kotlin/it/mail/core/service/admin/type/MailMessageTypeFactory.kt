package it.mail.core.service.admin.type

import it.mail.core.admin.api.type.CreateMailMessageTypeCommand
import it.mail.core.admin.api.type.MailMessageContentType.HTML
import it.mail.core.admin.api.type.MailMessageContentType.PLAIN_TEXT
import it.mail.core.exception.ValidationException
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.MailMessageType
import it.mail.core.model.PlainTextMailMessageType
import java.time.Instant

interface MailMessageTypeFactory<T : MailMessageType> {

    fun create(command: CreateMailMessageTypeCommand): T
}

class MailMessageTypeFactoryManager(
    private val plainTextMailMessageTypeFactory: PlainTextMailMessageTypeFactory,
    private val htmlMailMessageTypeFactory: HtmlMailMessageTypeFactory,
) : MailMessageTypeFactory<MailMessageType> {

    override fun create(command: CreateMailMessageTypeCommand) =
        when (command.contentType) {
            PLAIN_TEXT -> plainTextMailMessageTypeFactory.create(command)
            HTML -> htmlMailMessageTypeFactory.create(command)
        }
}

class PlainTextMailMessageTypeFactory : MailMessageTypeFactory<PlainTextMailMessageType> {

    override fun create(command: CreateMailMessageTypeCommand): PlainTextMailMessageType {
        val now = Instant.now()

        return PlainTextMailMessageType(
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
            createdAt = now,
            updatedAt = now,
        )
    }
}

class HtmlMailMessageTypeFactory : MailMessageTypeFactory<HtmlMailMessageType> {

    override fun create(command: CreateMailMessageTypeCommand): HtmlMailMessageType {
        val templateEngine = command.templateEngine
        val template = command.template

        if (templateEngine == null) {
            throw ValidationException("No template engine is specified for html mail message type")
        }

        if (template == null) {
            throw ValidationException("No template is specified for html mail message type")
        }

        val now = Instant.now()

        return HtmlMailMessageType(
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
            createdAt = now,
            updatedAt = now,
            templateEngine = templateEngine,
            template = template,
        )
    }
}
