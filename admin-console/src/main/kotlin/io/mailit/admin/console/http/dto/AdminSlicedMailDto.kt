package io.mailit.admin.console.http.dto

import io.mailit.value.MailState
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
    val status: MailState, // todo: rename to "state"
    val failedCount: Int,
)
