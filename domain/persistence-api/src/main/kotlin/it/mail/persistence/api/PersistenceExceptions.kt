package it.mail.persistence.api

open class PersistenceException(message: String?, cause: Exception? = null) : Exception(message, cause)

class DuplicateUniqueKeyException(message: String?, cause: Exception?) : PersistenceException(message, cause)
