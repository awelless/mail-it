package io.mailit.core.exception

open class PersistenceException(message: String?, cause: Exception? = null) : Exception(message, cause)

class DuplicateUniqueKeyException(message: String?, cause: Exception? = null) : PersistenceException(message, cause)
