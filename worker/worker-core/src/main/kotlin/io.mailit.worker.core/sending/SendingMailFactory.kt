package io.mailit.worker.core.sending

import io.mailit.template.api.TemplateProcessor
import io.mailit.worker.core.HtmlMail
import io.mailit.worker.core.Mail
import io.mailit.worker.core.PlainMail
import io.mailit.worker.spi.mailing.HtmlSendingMail
import io.mailit.worker.spi.mailing.PlainSendingMail

internal class SendingMailFactory(
    private val templateProcessor: TemplateProcessor,
) {
    suspend fun create(mail: Mail) = when (mail) {
        is PlainMail -> createPlainText(mail)
        is HtmlMail -> createHtml(mail)
    }

    private fun createPlainText(mail: PlainMail) = Result.success(
        PlainSendingMail(
            id = mail.id,
        subject = mail.subject,
        emailFrom = mail.emailFrom,
        emailTo = mail.emailTo,
        text = mail.text,
    ))

    private suspend fun createHtml(mail: HtmlMail): Result<HtmlSendingMail> {
        val html = templateProcessor.process(
            mailTypeId = mail.typeId,
            templateEngine = mail.templateEngine,
            data = mail.data,
        )

        return html.map {
            HtmlSendingMail(
                id = mail.id,
                subject = mail.subject,
                emailFrom = mail.emailFrom,
                emailTo = mail.emailTo,
                html = it,
            )
        }
    }
}
