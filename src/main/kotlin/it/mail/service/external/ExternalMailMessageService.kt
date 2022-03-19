package it.mail.service.external

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.repository.MailMessageRepository
import it.mail.repository.MailMessageTypeRepository
import it.mail.service.BadRequestException
import mu.KLogging
import java.time.Instant
import java.util.UUID.randomUUID
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class ExternalMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    @Transactional
    fun createNewMail(text: String, subject: String?, emailFrom: String, emailTo: String, messageTypeName: String): MailMessage {
        val messageType = mailMessageTypeRepository.findOneByName(messageTypeName)
            ?: throw BadRequestException("Invalid type: $messageTypeName is passed")

        val externalId = randomUUID().toString()

        val message = MailMessage(
            text = text,
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            externalId = externalId,
            type = messageType,
            createdAt = Instant.now(),
            status = PENDING,
        )

        mailMessageRepository.persist(message)

        logger.debug { "Persisted message with externalId: $externalId" }

        return message
    }
}
