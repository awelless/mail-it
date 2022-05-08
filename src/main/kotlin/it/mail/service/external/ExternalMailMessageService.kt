package it.mail.service.external

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.ValidationException
import it.mail.service.validators.isEmail
import mu.KLogging
import java.time.Instant

class ExternalMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    suspend fun createNewMail(text: String, subject: String?, emailFrom: String?, emailTo: String, mailMessageTypeId: Long): MailMessage {
        validateBeforeCreate(text, emailFrom, emailTo)

        val messageType = mailMessageTypeRepository.findById(mailMessageTypeId)
            ?: throw ValidationException("Invalid type: $mailMessageTypeId is passed")

        val message = MailMessage(
            text = text,
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            type = messageType,
            createdAt = Instant.now(),
            status = PENDING,
        )

        mailMessageRepository.create(message)

        logger.debug { "Persisted message with id: ${message.id}" }

        return message
    }

    private fun validateBeforeCreate(text: String, emailFrom: String?, emailTo: String) {
        if (text.isEmpty()) {
            throw ValidationException("text shouldn't be empty")
        }

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
