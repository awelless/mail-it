package io.mailit.core.service.mail.type

import io.mailit.core.api.admin.type.UpdateMailMessageTypeCommand
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.value.exception.ValidationException
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
        val templateEngine = command.templateEngine
        val template = command.template

        if (templateEngine == null) {
            throw ValidationException("No template engine is specified for html mail message type")
        }

        if (template == null) {
            throw ValidationException("No template is specified for html mail message type")
        }

        mailType.apply {
            description = command.description
            maxRetriesCount = command.maxRetriesCount
            updatedAt = Instant.now()
            this.templateEngine = templateEngine
            this.template = template
        }
    }
}
