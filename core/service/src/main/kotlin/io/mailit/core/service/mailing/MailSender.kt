package io.mailit.core.service.mailing

import io.mailit.core.model.MailMessage

interface MailSender {

    suspend fun send(message: MailMessage)
}
