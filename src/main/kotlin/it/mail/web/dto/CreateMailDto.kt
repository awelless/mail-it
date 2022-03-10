package it.mail.web.dto

import javax.validation.constraints.NotBlank

data class CreateMailDto(

    @field:NotBlank(message = "Text shouldn't be blank")
    val text: String?,

    val subject: String?,

    @field:NotBlank(message = "Email from shouldn't be blank")
    val from: String?,

    @field:NotBlank(message = "Email to shouldn't be blank")
    val to: String?,

    @field:NotBlank(message = "Type shouldn't be blank")
    val type: String?,
)