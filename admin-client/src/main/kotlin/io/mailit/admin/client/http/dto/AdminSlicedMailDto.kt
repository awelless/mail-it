package io.mailit.admin.client.http.dto

import io.mailit.core.model.MailMessageStatus
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant

@RegisterForReflection
data class AdminSlicedMailDto(
    val id: String,
    val emailFrom: String?,
    val emailTo: String,
    val type: IdNameDto,
    val createdAt: Instant,
    val sendingStartedAt: Instant?,
    val sentAt: Instant?,
    val status: MailMessageStatus,
    val failedCount: Int,
)
