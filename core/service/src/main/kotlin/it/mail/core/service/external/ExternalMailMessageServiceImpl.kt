package it.mail.core.service.external

import it.mail.core.exception.ValidationException
import it.mail.core.external.api.CreateMailCommand
import it.mail.core.external.api.ExternalMailMessageService
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus.PENDING
import it.mail.core.persistence.api.MailMessageRepository
import it.mail.core.persistence.api.MailMessageTypeRepository
import it.mail.core.service.isEmail
import mu.KLogging
import java.time.Instant

class ExternalMailMessageServiceImpl(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) : ExternalMailMessageService {
    companion object : KLogging()

    override suspend fun createNewMail(command: CreateMailCommand): MailMessage {
        val messageType = mailMessageTypeRepository.findById(command.mailMessageTypeId)
            ?: throw ValidationException("Invalid type: ${command.mailMessageTypeId} is passed")

        validateBeforeCreate(command.emailFrom, command.emailTo)

        val message = MailMessage(
            text = command.text,
            data = command.data,
            subject = command.subject,
            emailFrom = command.emailFrom,
            emailTo = command.emailTo,
            type = messageType,
            createdAt = Instant.now(),
            status = PENDING,
        )

        mailMessageRepository.create(message)

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
}
