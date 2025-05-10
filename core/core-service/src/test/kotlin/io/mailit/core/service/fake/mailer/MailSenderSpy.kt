package io.mailit.core.service.fake.mailer

import io.mailit.core.spi.mailer.Mail
import io.mailit.core.spi.mailer.MailSender
import java.util.concurrent.ConcurrentLinkedDeque

class MailSenderSpy : MailSender {

    private val _sentMails = ConcurrentLinkedDeque<Mail>()
    val sentMails
        get() = _sentMails.toList()

    override suspend fun send(mail: Mail) = Result.success(Unit).also { _sentMails += mail }
}
