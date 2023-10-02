package io.mailit.core.spi

import io.mailit.core.model.ApiKey

interface ApiKeyRepository {

    suspend fun findById(id: String): ApiKey?

    /**
     * Results are sorted by [ApiKey.createdAt], descending.
     */
    suspend fun findAll(): List<ApiKey>

    suspend fun create(apiKey: ApiKey)

    suspend fun delete(id: String): Boolean
}
