package it.mail.domain.core.mailing

import it.mail.domain.model.MailMessage

interface MailSender {

    suspend fun send(message: MailMessage)
}
