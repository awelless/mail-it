package io.mailit.core.spi.application

import io.mailit.core.model.application.ApiKey

interface ApiKeyRepository {

    suspend fun findById(id: String): ApiKey?

    /**
     * Returns all api keys for the [applicationId].
     * Results are sorted by [ApiKey.createdAt], descending
     */
    suspend fun findAll(applicationId: Long): List<ApiKey>

    suspend fun create(apiKey: ApiKey)

    suspend fun delete(applicationId: Long, id: String): Boolean
}
