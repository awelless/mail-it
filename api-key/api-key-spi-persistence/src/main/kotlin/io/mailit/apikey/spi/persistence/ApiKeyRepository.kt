package io.mailit.apikey.spi.persistence

import java.time.Instant

interface ApiKeyRepository {

    suspend fun findById(id: String): ApiKey?

    /**
     * Results are sorted by [ApiKey.createdAt], descending.
     */
    suspend fun findAll(): List<ApiKey>

    suspend fun create(apiKey: ApiKey): Result<Unit>

    /**
     * Deletes [ApiKey] by [id] if exists and returns true.
     */
    suspend fun delete(id: String): Boolean
}

data class ApiKey(
    val id: String,
    val name: String,
    /**
     * Encrypted secret.
     */
    val secret: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)
