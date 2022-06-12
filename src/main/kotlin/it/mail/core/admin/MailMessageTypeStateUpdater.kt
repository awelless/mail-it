package it.mail.core.admin

import it.mail.core.ValidationException
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.MailMessageType
import it.mail.core.model.PlainTextMailMessageType
import java.time.Instant

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
            updatedAt = Instant.now()
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
            updatedAt = Instant.now()
            templateEngine = command.templateEngine
            template = command.template
        }
    }
}
