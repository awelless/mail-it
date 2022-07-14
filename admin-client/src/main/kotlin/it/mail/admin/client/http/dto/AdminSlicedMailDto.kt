package it.mail.admin.client.http.dto

import it.mail.domain.model.MailMessageStatus
import java.time.Instant

data class AdminSlicedMailDto(

    var id: Long,
    val emailFrom: String?,
    val emailTo: String,
    val type: IdNameDto,
    val createdAt: Instant,
    var sendingStartedAt: Instant?,
    var sentAt: Instant?,
    var status: MailMessageStatus,
    var failedCount: Int,
)
