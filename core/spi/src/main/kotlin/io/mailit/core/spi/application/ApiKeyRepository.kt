package io.mailit.core.spi.application

import io.mailit.core.model.application.ApiKey

interface ApiKeyRepository {

    suspend fun findById(id: String): ApiKey?

    suspend fun findAll(applicationId: Long): List<ApiKey>

    suspend fun create(apiKey: ApiKey)

    suspend fun delete(id: String)
}
