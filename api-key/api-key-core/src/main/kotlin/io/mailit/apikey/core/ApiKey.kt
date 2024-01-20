package io.mailit.apikey.core

import io.mailit.apikey.api.ApiKey as ApiApiKey
import io.mailit.apikey.spi.persistence.ApiKey as PersistenceApiKey
import io.mailit.apikey.spi.security.SecretHasher
import java.time.Instant

internal class ApiKey(
    val id: String,
    val name: String,
    /**
     * Encrypted secret.
     */
    private val secret: ApiKeySecret,
    private val createdAt: Instant,
    private val expiresAt: Instant,
) {
    fun isNotExpired(now: Instant) = expiresAt >= now

    fun matchesSecret(secretHasher: SecretHasher, rawSecret: RawSecret) = secretHasher.matches(rawSecret.value, secret.hashedValue)

    fun toApiModel() = ApiApiKey(
        id = id,
        name = name,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )

    fun toPersistenceModel() = PersistenceApiKey(
        id = id,
        name = name,
        secret = secret.hashedValue,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )

    companion object {
        fun fromPersistenceModel(apiKey: PersistenceApiKey) = with(apiKey) {
            ApiKey(
                id = id,
                name = name,
                secret = ApiKeySecret(secret),
                createdAt = createdAt,
                expiresAt = expiresAt,
            )
        }
    }
}

@JvmInline
value class ApiKeySecret(val hashedValue: String)
