package it.mail.service.external

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.BadRequestException
import mu.KLogging
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ExternalMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    // @Transactional // todo transactions don't work
    suspend fun createNewMail(text: String, subject: String?, emailFrom: String, emailTo: String, mailMessageTypeId: Long): MailMessage {
        val messageType = mailMessageTypeRepository.findById(mailMessageTypeId)
            ?: throw BadRequestException("Invalid type: $mailMessageTypeId is passed")

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
}
