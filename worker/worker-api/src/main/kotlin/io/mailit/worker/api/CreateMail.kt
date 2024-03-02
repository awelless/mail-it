package io.mailit.worker.api

import io.mailit.value.EmailAddress

interface CreateMail {

    suspend operator fun invoke(request: CreateMailRequest): Result<Unit>
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
