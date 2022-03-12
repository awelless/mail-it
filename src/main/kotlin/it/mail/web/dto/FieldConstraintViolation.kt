package it.mail.web.dto

data class FieldConstraintViolation(
    val fieldName: String,
    val errorMessage: String,
)
