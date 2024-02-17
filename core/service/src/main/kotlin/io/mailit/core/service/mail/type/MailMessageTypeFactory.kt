package io.mailit.core.service.mail.type

import io.mailit.core.admin.api.type.CreateMailMessageTypeCommand
import io.mailit.core.admin.api.type.MailMessageContentType.HTML
import io.mailit.core.admin.api.type.MailMessageContentType.PLAIN_TEXT
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.idgenerator.api.IdGenerator
import io.mailit.value.MailTypeId
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

class PlainTextMailMessageTypeFactory(
    private val idGenerator: IdGenerator,
) : MailMessageTypeFactory<PlainTextMailMessageType> {

    override fun create(command: CreateMailMessageTypeCommand): PlainTextMailMessageType {
        val now = Instant.now()

        return PlainTextMailMessageType(
            id = MailTypeId(idGenerator.generateId()),
            name = command.name,
            description = command.description,
            maxRetriesCount = command.maxRetriesCount,
            createdAt = now,
            updatedAt = now,
        )
    }
}

class HtmlMailMessageTypeFactory(
    private val idGenerator: IdGenerator,
) : MailMessageTypeFactory<HtmlMailMessageType> {

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
            id = MailTypeId(idGenerator.generateId()),
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
