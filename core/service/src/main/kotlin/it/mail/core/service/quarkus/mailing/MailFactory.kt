package it.mail.core.service.quarkus.mailing

import io.quarkus.mailer.Mail
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.MailMessage
import it.mail.core.model.PlainTextMailMessageType
import it.mail.core.service.mailing.templates.TemplateProcessor

class MailFactory(
    private val templateProcessor: TemplateProcessor,
) {
    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }

    fun create(mailMessage: MailMessage): Mail {
        val mail = when (val type = mailMessage.type) {
            is PlainTextMailMessageType -> plainTextMessage(mailMessage)
            is HtmlMailMessageType -> htmlMessage(mailMessage, type)
        }

        if (!mailMessage.emailFrom.isNullOrBlank()) {
            mail.from = mailMessage.emailFrom
        }

        // todo allow to configure domain
        mail.addHeader(MESSAGE_ID_HEADER, "${mailMessage.id}@mail-it.io")

        return mail
    }

    private fun plainTextMessage(mailMessage: MailMessage) =
        Mail.withText(mailMessage.emailTo, mailMessage.subject, mailMessage.text)

    private fun htmlMessage(mailMessage: MailMessage, mailMessageType: HtmlMailMessageType): Mail {
        val htmlMessage = templateProcessor.process(
            mailMessageType = mailMessageType,
            data = mailMessage.data.orEmpty(),
        )

        return Mail.withHtml(mailMessage.emailTo, mailMessage.subject, htmlMessage)
    }
}
