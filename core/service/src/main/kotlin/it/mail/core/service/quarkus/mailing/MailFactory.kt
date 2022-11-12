package it.mail.core.service.quarkus.mailing

import io.quarkus.mailer.Mail
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.MailMessage
import it.mail.core.model.PlainTextMailMessageType
import it.mail.core.service.mailing.templates.TemplateProcessor

interface MailFactory {

    fun create(mailMessage: MailMessage): Mail
}

class MailFactoryManager(
    private val plainTextMailFactory: PlainTextMailFactory,
    private val htmlMailFactory: HtmlMailFactory,
) : MailFactory {

    override fun create(mailMessage: MailMessage): Mail {
        val type = mailMessage.type
        return when (type) {
            is PlainTextMailMessageType -> plainTextMailFactory.create(mailMessage)
            is HtmlMailMessageType -> htmlMailFactory.create(mailMessage, type)
        }
    }
}

class PlainTextMailFactory {

    fun create(mailMessage: MailMessage): Mail {
        val mail = Mail.withText(mailMessage.emailTo, mailMessage.subject, mailMessage.text)

        if (!mailMessage.emailFrom.isNullOrBlank()) {
            mail.from = mailMessage.emailFrom
        }

        return mail
    }
}

class HtmlMailFactory(
    private val templateProcessor: TemplateProcessor,
) {

    fun create(mailMessage: MailMessage, mailMessageType: HtmlMailMessageType): Mail {
        val htmlMessage = templateProcessor.process(
            mailMessageType = mailMessageType,
            data = mailMessage.data ?: emptyMap()
        )

        val mail = Mail.withHtml(mailMessage.emailTo, mailMessage.subject, htmlMessage)

        if (!mailMessage.emailFrom.isNullOrBlank()) {
            mail.from = mailMessage.emailFrom
        }

        return mail
    }
}
