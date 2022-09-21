package it.mail.exception

open class ApplicationException(message: String) : Exception(message) {

    override val message: String
        get() = super.message!! // message is always presents
}

class ValidationException(message: String) : ApplicationException(message)

class NotFoundException(message: String) : ApplicationException(message)
