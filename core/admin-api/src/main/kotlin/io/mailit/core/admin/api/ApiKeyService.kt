package io.mailit.core.admin.api

import io.mailit.core.model.ApiKey
import io.mailit.core.model.ApiKeyToken
import kotlin.time.Duration

interface ApiKeyService {

    suspend fun generate(command: CreateApiKeyCommand): ApiKeyToken

    suspend fun getAll(): List<ApiKey>

    suspend fun delete(id: String)
}

data class CreateApiKeyCommand(
    val name: String,
    val expiration: Duration,
)
