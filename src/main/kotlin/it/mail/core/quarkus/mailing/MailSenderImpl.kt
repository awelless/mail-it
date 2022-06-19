package it.mail.core.quarkus.mailing

import it.mail.core.mailing.MailSender
import it.mail.core.model.MailMessage

class MailSenderImpl(
    private val mailFactory: MailFactory,
    private val mailSender: QuarkusMailSender,
) : MailSender {

    override suspend fun send(message: MailMessage) {
        val mail = mailFactory.create(message)
        mailSender.send(mail)
    }
}
