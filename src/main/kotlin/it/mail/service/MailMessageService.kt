package it.mail.service

import it.mail.domain.MailMessage
import it.mail.repository.MailMessageRepository
import mu.KLogging
import java.util.UUID.randomUUID
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object: KLogging()

    @Transactional
    fun createNewMail(text: String, subject: String?, emailFrom: String, emailTo: String): MailMessage {
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