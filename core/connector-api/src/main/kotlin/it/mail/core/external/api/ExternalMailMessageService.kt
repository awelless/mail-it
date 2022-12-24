package it.mail.core.external.api

import it.mail.core.model.MailMessage

interface ExternalMailMessageService {

    suspend fun createNewMail(command: CreateMailCommand): MailMessage
}

data class CreateMailCommand(
    val text: String?,
    val data: Map<String, Any?>?,
    val subject: String?,
    val emailFrom: String?,
    val emailTo: String,
    val mailType: String,
)
