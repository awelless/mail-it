package it.mail.persistence.context

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "application.database")
internal interface DatabaseConfig {

    fun type(): DatabaseType
}

internal enum class DatabaseType {

    H2,
    POSTGRESQL,
}
