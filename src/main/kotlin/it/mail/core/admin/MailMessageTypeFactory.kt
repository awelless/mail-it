package it.mail.core.admin

import it.mail.core.ValidationException
import it.mail.core.admin.MailMessageContentType.HTML
import it.mail.core.admin.MailMessageContentType.PLAIN_TEXT
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.MailMessageType
import it.mail.core.model.PlainTextMailMessageType

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

    override fun create(command: CreateMailMessageTypeCommand) =
        PlainTextMailMessageType(
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
        )
}

class HtmlMailMessageTypeFactory : MailMessageTypeFactory<HtmlMailMessageType> {

    override fun create(command: CreateMailMessageTypeCommand): HtmlMailMessageType {
        if (command.templateEngine == null) {
            throw ValidationException("No template engine is specified for html mail message type")
        }

        if (command.template == null) {
            throw ValidationException("No template is specified for html mail message type")
        }

        return HtmlMailMessageType(
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
            templateEngine = command.templateEngine,
            template = command.template,
        )
    }
}
