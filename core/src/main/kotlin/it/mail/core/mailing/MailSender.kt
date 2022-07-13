package it.mail.core.mailing

import it.mail.core.model.MailMessage

interface MailSender {

    suspend fun send(message: MailMessage)
}
