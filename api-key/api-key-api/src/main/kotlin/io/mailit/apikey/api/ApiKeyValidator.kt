package io.mailit.apikey.api

interface ApiKeyValidator {

    /**
     * Validates passed [token] and extracts ApiKey name from an ApiKey the [token] represents.
     *
     * If passed token is invalid, [Result] with [InvalidApiKeyException] is returned.
     */
    suspend fun validate(token: String): Result<String>
}

class InvalidApiKeyException : Exception("API Key is invalid")
