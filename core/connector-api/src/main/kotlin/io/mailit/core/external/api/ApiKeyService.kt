package io.mailit.core.external.api

import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.ApiKeyToken
import io.mailit.core.model.application.Application

interface ApiKeyService {

    /**
     * Validates passed [token] and extracts [Application] from a dedicated [ApiKey].
     *
     * If passed token is invalid, [InvalidApiKeyException] is thrown
     */
    suspend fun validate(token: ApiKeyToken): Application
}

class InvalidApiKeyException : Exception("API Key is invalid")
