package it.mail.core.service.quarkus.mailing

import it.mail.core.model.MailMessage
import it.mail.core.service.mailing.MailSender

class MailSenderImpl(
    private val mailFactory: MailFactory,
    private val mailSender: QuarkusMailSender,
) : MailSender {

    override suspend fun send(message: MailMessage) {
        val mail = mailFactory.create(message)
        mailSender.send(mail)
    }
}
