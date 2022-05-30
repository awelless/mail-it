package it.mail.service.admin

import it.mail.domain.HtmlMailMessageType
import it.mail.domain.MailMessageType
import it.mail.domain.PlainTextMailMessageType
import it.mail.service.ValidationException

interface MailMessageTypeStateUpdater<T : MailMessageType> {

    fun update(mailType: T, command: UpdateMailMessageTypeCommand)
}

class MailMessageTypeStateUpdaterManager(
    private val plainTextMailMessageTypeStateUpdater: PlainTextMailMessageTypeStateUpdater,
    private val htmlMailMessageTypeStateUpdater: HtmlMailMessageTypeStateUpdater,
) : MailMessageTypeStateUpdater<MailMessageType> {

    override fun update(mailType: MailMessageType, command: UpdateMailMessageTypeCommand) {
        when (mailType) {
            is PlainTextMailMessageType -> plainTextMailMessageTypeStateUpdater.update(mailType, command)
            is HtmlMailMessageType -> htmlMailMessageTypeStateUpdater.update(mailType, command)
        }
    }
}

class PlainTextMailMessageTypeStateUpdater : MailMessageTypeStateUpdater<PlainTextMailMessageType> {

    override fun update(mailType: PlainTextMailMessageType, command: UpdateMailMessageTypeCommand) {
        mailType.apply {
            description = command.description
            maxRetriesCount = command.maxRetriesCount
        }
    }
}

class HtmlMailMessageTypeStateUpdater : MailMessageTypeStateUpdater<HtmlMailMessageType> {

    override fun update(mailType: HtmlMailMessageType, command: UpdateMailMessageTypeCommand) {
        if (command.templateEngine == null) {
            throw ValidationException("No template engine is specified for html mail message type")
        }

        if (command.template == null) {
            throw ValidationException("No template is specified for html mail message type")
        }

        mailType.apply {
            description = command.description
            maxRetriesCount = command.maxRetriesCount
            templateEngine = command.templateEngine
            template = command.template
        }
    }
}
