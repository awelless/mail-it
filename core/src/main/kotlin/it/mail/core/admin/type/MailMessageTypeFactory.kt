package it.mail.core.admin.type

import it.mail.core.ValidationException
import it.mail.core.admin.type.MailMessageContentType.HTML
import it.mail.core.admin.type.MailMessageContentType.PLAIN_TEXT
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
        if (command.templateEngine == null) {
            throw ValidationException("No template engine is specified for html mail message type")
        }

        if (command.template == null) {
            throw ValidationException("No template is specified for html mail message type")
        }

        val now = Instant.now()

        return HtmlMailMessageType(
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
            createdAt = now,
            updatedAt = now,
            templateEngine = command.templateEngine,
            template = command.template,
        )
    }
}
