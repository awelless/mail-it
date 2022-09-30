package it.mail.admin.client.http.dto

import it.mail.domain.model.MailMessageStatus
import java.time.Instant

data class AdminSlicedMailDto(

    val id: Long,
    val emailFrom: String?,
    val emailTo: String,
    val type: IdNameDto,
    val createdAt: Instant,
    val sendingStartedAt: Instant?,
    val sentAt: Instant?,
    val status: MailMessageStatus,
    val failedCount: Int,
)
