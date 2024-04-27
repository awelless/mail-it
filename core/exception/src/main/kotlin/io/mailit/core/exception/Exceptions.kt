package io.mailit.core.exception

open class ApplicationException(message: String, cause: Exception? = null) : Exception(message, cause) {

    override val message: String
        get() = super.message!! // Message is always present.

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApplicationException) return false
        return message == other.message
    }

    override fun hashCode() = message.hashCode()
}

class ValidationException(message: String, cause: Exception? = null) : ApplicationException(message, cause)

class NotFoundException(message: String) : ApplicationException(message)
