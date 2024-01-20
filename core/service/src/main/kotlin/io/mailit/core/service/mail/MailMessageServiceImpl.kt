package io.mailit.core.service.mail

import io.mailit.core.admin.api.mail.MailMessageService
import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.exception.ValidationException
import io.mailit.core.external.api.CreateMailCommand
import io.mailit.core.external.api.MailMessageService as ConnectorMailMessageService
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.Slice
import io.mailit.core.service.isEmail
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.idgenerator.api.IdGenerator
import java.time.Instant
import mu.KLogging

class MailMessageServiceImpl(
    private val idGenerator: IdGenerator,
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) : MailMessageService, ConnectorMailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)

    override suspend fun createNewMail(command: CreateMailCommand): MailMessage {
        val messageType = mailMessageTypeRepository.findByName(command.mailType)
            ?: throw ValidationException("Invalid type: ${command.mailType} is passed")

        validateBeforeCreate(command.emailFrom, command.emailTo)

        val message = MailMessage(
            id = idGenerator.generateId(),
            text = command.text,
            data = command.data,
            subject = command.subject,
            emailFrom = command.emailFrom,
            emailTo = command.emailTo,
            type = messageType,
            createdAt = Instant.now(),
            status = MailMessageStatus.PENDING,
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

    private fun validateBeforeCreate(emailFrom: String?, emailTo: String) {
        if (emailFrom?.isEmail() == false) {
            throw ValidationException("emailFrom is incorrect")
        }

        if (emailTo.isBlank()) {
            throw ValidationException("emailTo shouldn't be blank")
        }

        if (!emailTo.isEmail()) {
            throw ValidationException("emailTo is incorrect")
        }
    }

    companion object : KLogging()
}
