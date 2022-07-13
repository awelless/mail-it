package it.mail.domain.core.external

import it.mail.domain.core.ValidationException
import it.mail.domain.core.isEmail
import it.mail.domain.model.MailMessage
import it.mail.domain.model.MailMessageStatus.PENDING
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import mu.KLogging
import java.time.Instant

class ExternalMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    suspend fun createNewMail(text: String?, data: Map<String, Any?>?, subject: String?, emailFrom: String?, emailTo: String, mailMessageTypeId: Long): MailMessage {
        val messageType = mailMessageTypeRepository.findById(mailMessageTypeId)
            ?: throw ValidationException("Invalid type: $mailMessageTypeId is passed")

        validateBeforeCreate(emailFrom, emailTo)

        val message = MailMessage(
            text = text,
            data = data,
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
