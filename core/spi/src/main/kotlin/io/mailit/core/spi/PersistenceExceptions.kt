package io.mailit.core.spi

// todo sealed class and better exception hierarchy
open class PersistenceException(message: String?, cause: Exception? = null) : Exception(message, cause)

class DuplicateUniqueKeyException(message: String?, cause: Exception?) : PersistenceException(message, cause)
