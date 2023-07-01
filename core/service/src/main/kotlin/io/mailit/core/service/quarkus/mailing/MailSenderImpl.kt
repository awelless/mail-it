package io.mailit.core.service.quarkus.mailing

import io.mailit.core.model.MailMessage
import io.mailit.core.service.mail.sending.MailSender

class MailSenderImpl(
    private val mailFactory: MailFactory,
    private val mailSender: QuarkusMailSender,
) : MailSender {

    override suspend fun send(message: MailMessage) {
        val mail = mailFactory.create(message)
        mailSender.send(mail)
    }
}
