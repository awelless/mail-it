package io.mailit.value.exception

sealed class ApplicationException(message: String, cause: Exception? = null) : Exception(message, cause) {

    override val message: String
        get() = super.message!! // Message is always present.
}

class ValidationException(message: String, cause: Exception? = null) : ApplicationException(message, cause)

class NotFoundException(message: String) : ApplicationException(message)
