package it.mail.web.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class CreateMailDto(

        @field:NotBlank(message = "Text shouldn't be blank")
        val text: String?,

        val subject: String?,

        @field:NotBlank(message = "Email from shouldn't be blank")
        @field:Email(message = "Invalid email from")
        val from: String?,

        @field:NotBlank(message = "Email to shouldn't be blank")
        @field:Email(message = "Invalid email to")
        val to: String?,

        @field:NotBlank(message = "Type shouldn't be blank")
        val type: String?,
)