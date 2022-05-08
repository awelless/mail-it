package it.mail.web.dto

data class CreateMailDto(

    val text: String,
    val subject: String?,
    val from: String?,
    val to: String,
    val typeId: Long,
)
