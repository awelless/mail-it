package it.mail.web.dto

// todo require non nulls
data class CreateMailDto(

    val text: String?,
    val subject: String?,
    val from: String?,
    val to: String?,
    val typeId: Long?,
)
