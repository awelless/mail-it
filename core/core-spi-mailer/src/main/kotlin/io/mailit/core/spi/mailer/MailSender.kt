package io.mailit.core.spi.mailer

import io.mailit.value.EmailAddress

interface MailSender {
    suspend fun send(mail: Mail): Result<Unit>
}

data class Mail(
    val emailTo: EmailAddress,
    val emailFrom: EmailAddress?,
    val subject: String?,
    val content: MailContent,
    val headers: Map<String, List<String>>,
)

sealed interface MailContent {
    data class Text(val text: String) : MailContent
    data class Html(val html: String) : MailContent
}
