package io.mailit.core.external.api

import io.mailit.core.model.MailMessage

interface MailMessageService {

    suspend fun createNewMail(command: CreateMailCommand): MailMessage
}

data class CreateMailCommand(
    val text: String?,
    val data: Map<String, Any?>?,
    val subject: String?,
    val emailFrom: String?,
    val emailTo: String,
    val mailType: String,
    val deduplicationId: String?,
)
