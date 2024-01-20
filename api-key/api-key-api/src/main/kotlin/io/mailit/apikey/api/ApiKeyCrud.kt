package io.mailit.apikey.api

import java.time.Instant
import kotlin.time.Duration

interface ApiKeyCrud {

    /**
     * Generates a new ApiKey. Returns a token used for authentication.
     */
    suspend fun generate(command: CreateApiKeyCommand): Result<String>

    suspend fun getAll(): List<ApiKey>

    suspend fun delete(id: String): Result<Unit>
}

data class CreateApiKeyCommand(
    val name: String,
    val expiration: Duration,
)

data class ApiKey(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)
