package io.mailit.worker.spi.mailing

import io.mailit.value.EmailAddress
import io.mailit.value.MailId

interface MailSender {

    suspend fun send(mail: SendingMail): Result<Unit>
}

sealed interface SendingMail {
    val id: MailId
    val subject: String?
    val emailFrom: EmailAddress?
    val emailTo: EmailAddress
}

data class PlainSendingMail(
    override val id: MailId,
    override val subject: String?,
    override val emailFrom: EmailAddress?,
    override val emailTo: EmailAddress,

    val text: String,
) : SendingMail

data class HtmlSendingMail(
    override val id: MailId,
    override val subject: String?,
    override val emailFrom: EmailAddress?,
    override val emailTo: EmailAddress,

    val html: String,
) : SendingMail
