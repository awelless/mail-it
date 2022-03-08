package it.mail.service

import it.mail.domain.MailMessage
import it.mail.repository.MailMessageRepository
import mu.KLogging
import java.util.UUID.randomUUID
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object: KLogging()

    fun createNewMessage(text: String, subject: String?, emailFrom: String, emailTo: String): MailMessage {
        val externalId = randomUUID().toString()

        val message = MailMessage(
            text = text,
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            externalId = externalId,
        )

        mailMessageRepository.persist(message)

        logger.debug { "Persisted message: $externalId" }

        return message
    }
}