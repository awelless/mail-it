package io.mailit.core.external.api

import io.mailit.core.model.ApiKey
import io.mailit.core.model.ApiKeyToken

interface ApiKeyService {

    /**
     * Validates passed [token] and extracts [ApiKey.name] from a dedicated [ApiKey].
     *
     * If passed token is invalid, [InvalidApiKeyException] is thrown
     */
    suspend fun validate(token: ApiKeyToken): String
}

class InvalidApiKeyException : Exception("API Key is invalid")
