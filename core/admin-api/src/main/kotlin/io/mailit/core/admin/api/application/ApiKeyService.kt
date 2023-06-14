package io.mailit.core.admin.api.application

import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.ApiKeyToken
import kotlin.time.Duration

interface ApiKeyService {

    suspend fun generate(command: CreateApiKeyCommand): ApiKeyToken

    suspend fun getAll(applicationId: Long): List<ApiKey>

    suspend fun delete(applicationId: Long, id: String)
}

data class CreateApiKeyCommand(
    val applicationId: Long,
    val name: String,
    val expiration: Duration,
)
