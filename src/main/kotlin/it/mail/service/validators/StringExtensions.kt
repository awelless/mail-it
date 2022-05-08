package it.mail.service.validators

private val EMAIL_ADDRESS_REGEX = Regex("[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")

fun String.isEmail() = matches(EMAIL_ADDRESS_REGEX)
