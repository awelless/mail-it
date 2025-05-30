package io.mailit.core.service.mail

import io.mailit.core.api.admin.mail.MailMessageService
import io.mailit.core.api.connector.CreateMailRequest
import io.mailit.core.api.connector.MailMessageService as ConnectorMailMessageService
import io.mailit.core.model.MailMessage
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.idgenerator.api.IdGenerator
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.value.Slice
import io.mailit.value.exception.DuplicateUniqueKeyException
import io.mailit.value.exception.ValidationException
import java.time.Instant
import mu.KLogging

class MailMessageServiceImpl(
    private val idGenerator: IdGenerator,
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) : MailMessageService, ConnectorMailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)

    override suspend fun createNewMail(command: CreateMailRequest): MailMessage {
        val messageType = mailMessageTypeRepository.findByName(command.mailTypeName)
            ?: throw ValidationException("Invalid type: ${command.mailTypeName} is passed")

        val message = MailMessage(
            id = MailId(idGenerator.generateId()),
            text = command.text,
            data = command.data,
            subject = command.subject,
            emailFrom = command.emailFrom,
            emailTo = command.emailTo,
            type = messageType,
            createdAt = Instant.now(),
            state = MailState.PENDING,
            deduplicationId = command.deduplicationId,
        )

        try {
            mailMessageRepository.create(message)
        } catch (_: DuplicateUniqueKeyException) {
            logger.debug { "Mail with deduplication id: ${command.deduplicationId} has already been created" }
        }

        logger.debug { "Persisted message with id: ${message.id}" }

        return message
    }

    companion object : KLogging()
}
