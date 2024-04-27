package io.mailit.worker.quarkus

import io.mailit.core.exception.ApplicationException
import io.mailit.template.api.TemplateProcessor
import io.mailit.worker.spi.mailing.HtmlSendingMail
import io.mailit.worker.spi.mailing.MailSender
import io.mailit.worker.spi.mailing.PlainSendingMail
import io.mailit.worker.spi.mailing.SendingMail
import io.quarkus.mailer.Mail
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.coroutines.awaitSuspending

class QuarkusMailSender(
    private val mailer: ReactiveMailer,
) : MailSender {

    override suspend fun send(mail: SendingMail): Result<Unit> =
        mailer.send(mail.toQuarkusMail())
            .onItem().transform { Result.success(Unit) }
            .onFailure(Exception::class.java).recoverWithItem { err -> Result.failure(SenderException("Failed to send Mail: ${mail.id.value}", err as Exception)) }
            .awaitSuspending()

    private fun SendingMail.toQuarkusMail(): Mail {
        val quarkusMail = when (this) {
            is PlainSendingMail -> Mail.withText(emailTo.email, subject, text)
            is HtmlSendingMail -> Mail.withHtml(emailTo.email, subject, html)
        }

        emailFrom?.let {
            quarkusMail.from = it.email
        }

        // todo allow to configure domain
        quarkusMail.addHeader(MESSAGE_ID_HEADER, "${id.value}@mail-it.io")

        return quarkusMail
    }

    private class SenderException(message: String, cause: Exception) : ApplicationException(message, cause)

    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }
}
