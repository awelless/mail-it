package it.mail.core.service.mailing

import it.mail.core.model.MailMessage

interface MailSender {

    suspend fun send(message: MailMessage)
}
