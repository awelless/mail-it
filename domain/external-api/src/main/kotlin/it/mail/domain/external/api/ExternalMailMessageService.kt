package it.mail.domain.external.api

import it.mail.domain.model.MailMessage

interface ExternalMailMessageService {

    suspend fun createNewMail(command: CreateMailCommand): MailMessage
}

data class CreateMailCommand(
    val text: String?,
    val data: Map<String, Any?>?,
    val subject: String?,
    val emailFrom: String?,
    val emailTo: String,
    val mailMessageTypeId: Long, // todo replace with mail type name?
)
