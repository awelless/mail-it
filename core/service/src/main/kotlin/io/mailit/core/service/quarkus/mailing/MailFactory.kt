package io.mailit.core.service.quarkus.mailing

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessage
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.template.api.TemplateProcessor
import io.quarkus.mailer.Mail

class MailFactory(
    private val templateProcessor: TemplateProcessor,
) {
    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }

    suspend fun create(mailMessage: MailMessage): Mail {
        val mail = when (val type = mailMessage.type) {
            is PlainTextMailMessageType -> plainTextMessage(mailMessage)
            is HtmlMailMessageType -> htmlMessage(mailMessage, type)
        }

        mailMessage.emailFrom?.let {
            mail.from = it.email
        }

        // todo allow to configure domain
        mail.addHeader(MESSAGE_ID_HEADER, "${mailMessage.id}@mail-it.io")

        return mail
    }

    private fun plainTextMessage(mailMessage: MailMessage) =
        Mail.withText(mailMessage.emailTo.email, mailMessage.subject, mailMessage.text)

    private suspend fun htmlMessage(mailMessage: MailMessage, mailMessageType: HtmlMailMessageType): Mail {
        val htmlMessage = templateProcessor.process(
            mailTypeId = mailMessageType.id,
            templateEngine = mailMessageType.templateEngine,
            data = mailMessage.data.orEmpty(),
        )

        return Mail.withHtml(mailMessage.emailTo.email, mailMessage.subject, htmlMessage.getOrThrow())
    }
}
