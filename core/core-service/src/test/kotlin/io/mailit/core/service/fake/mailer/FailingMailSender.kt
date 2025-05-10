package io.mailit.core.service.fake.mailer

import io.mailit.core.spi.mailer.Mail
import io.mailit.core.spi.mailer.MailSender

object FailingMailSender : MailSender {
    override suspend fun send(mail: Mail): Result<Unit> = Result.failure(RuntimeException("Error from FailingMailSender"))
}
