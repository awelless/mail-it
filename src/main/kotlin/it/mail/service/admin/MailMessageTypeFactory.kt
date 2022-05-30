package it.mail.service.admin

import it.mail.domain.HtmlMailMessageType
import it.mail.domain.MailMessageType
import it.mail.domain.PlainTextMailMessageType
import it.mail.service.ValidationException
import it.mail.service.admin.MailMessageContentType.HTML
import it.mail.service.admin.MailMessageContentType.PLAIN_TEXT

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
