package io.mailit.connector.http.web

data class CreateMailDto(
    val text: String?,
    val data: Map<String, Any>?,
    val subject: String?,
    val emailFrom: String?,
    val emailTo: String,
    val mailType: String,
    val deduplicationId: String?,
)
