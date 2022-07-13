package it.mail.web.dto

import it.mail.core.model.MailMessageStatus
import java.time.Instant

data class CreateMailDto(

    val text: String?,
    val data: Map<String, Any?>?,
    val subject: String?,
    val from: String?,
    val to: String,
    val typeId: Long,
)

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
