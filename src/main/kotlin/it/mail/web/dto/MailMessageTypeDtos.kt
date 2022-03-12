package it.mail.web.dto

data class MailMessageTypeResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
)

data class MailMessageTypeCreateDto(
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
)

data class MailMessageTypeUpdateDto(
    val description: String?,
    val maxRetriesCount: Int?,
)
