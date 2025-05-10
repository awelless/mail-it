package io.mailit.core.api.connector

import io.mailit.core.model.MailMessage
import io.mailit.value.EmailAddress

interface MailMessageService {

    suspend fun createNewMail(command: CreateMailRequest): MailMessage
}

data class CreateMailRequest(
    val text: String?,
    val data: Map<String, Any>?,
    val subject: String?,
    val emailFrom: EmailAddress?,
    val emailTo: EmailAddress,
    val mailTypeName: String,
    val deduplicationId: String?,
)
