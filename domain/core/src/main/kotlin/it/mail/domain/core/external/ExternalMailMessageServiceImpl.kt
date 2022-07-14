package it.mail.domain.core.external

import it.mail.domain.core.isEmail
import it.mail.domain.external.api.CreateMailCommand
import it.mail.domain.external.api.ExternalMailMessageService
import it.mail.domain.model.MailMessage
import it.mail.domain.model.MailMessageStatus.PENDING
import it.mail.exception.ValidationException
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
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
