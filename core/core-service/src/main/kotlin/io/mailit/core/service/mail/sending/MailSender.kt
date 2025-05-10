package io.mailit.core.service.mail.sending

import io.mailit.core.model.MailMessage

interface MailSender {

    suspend fun send(message: MailMessage)
}
