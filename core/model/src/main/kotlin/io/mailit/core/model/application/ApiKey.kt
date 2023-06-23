package io.mailit.core.model.application

import java.time.Instant

data class ApiKey(
    val id: String,
    val name: String,
    /**
     * Encrypted secret
     */
    val secret: String,
    val application: Application,
    val createdAt: Instant,
    val expiresAt: Instant,
) {
    fun isExpired(time: Instant) = expiresAt < time
}

@JvmInline
value class ApiKeyToken(val value: String)
