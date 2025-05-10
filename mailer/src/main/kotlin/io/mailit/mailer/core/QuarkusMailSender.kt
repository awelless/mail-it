package io.mailit.mailer.core

import io.mailit.core.spi.mailer.Mail
import io.mailit.core.spi.mailer.MailContent
import io.mailit.core.spi.mailer.MailSender
import io.quarkus.mailer.Mail as QuarkusMail
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.inject.Singleton

@Singleton
internal class QuarkusMailSender(
    private val mailer: ReactiveMailer,
) : MailSender {

    override suspend fun send(mail: Mail): Result<Unit> =
        mailer.send(mail.toQuarkusMail())
            .map { Result.success(Unit) }
            .onFailure().recoverWithItem { e -> Result.failure(e) }
            .awaitSuspending()

    private fun Mail.toQuarkusMail(): QuarkusMail {
        val mail = when (val c = content) {
            is MailContent.Text -> QuarkusMail.withText(emailTo.email, subject, c.text)
            is MailContent.Html -> QuarkusMail.withText(emailTo.email, subject, c.html)
        }

        return mail.also {
            it.from = emailFrom?.email
            it.headers = headers
        }
    }
}
