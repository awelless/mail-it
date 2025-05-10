package io.mailit.core.service.mail.sending

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessage
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.spi.mailer.Mail
import io.mailit.core.spi.mailer.MailContent
import io.mailit.template.api.TemplateProcessor

class MailFactory(
    private val templateProcessor: TemplateProcessor,
) {
    suspend fun create(mailMessage: MailMessage): Result<Mail> {
        val content = when (val type = mailMessage.type) {
            is PlainTextMailMessageType -> textContent(mailMessage)
            is HtmlMailMessageType -> htmlContent(mailMessage, type)
        }

        return content.map { toMail(mailMessage, it) }
    }

    private fun textContent(mailMessage: MailMessage) = Result.success(MailContent.Text(mailMessage.text.orEmpty()))

    private suspend fun htmlContent(mailMessage: MailMessage, mailMessageType: HtmlMailMessageType): Result<MailContent> {
        val htmlContent = templateProcessor.process(
            mailTypeId = mailMessageType.id,
            templateEngine = mailMessageType.templateEngine,
            data = mailMessage.data.orEmpty(),
        )

        return htmlContent.map { MailContent.Html(it) }
    }

    private fun toMail(mailMessage: MailMessage, content: MailContent) = with(mailMessage) {
        Mail(
            emailTo = emailTo,
            emailFrom = emailFrom,
            subject = subject,
            content = content,
            // Todo allow to configure domain.
            headers = mapOf(MESSAGE_ID_HEADER to listOf("${mailMessage.id}@mail-it.io")),
        )
    }

    companion object {
        private const val MESSAGE_ID_HEADER = "Message-ID"
    }
}
