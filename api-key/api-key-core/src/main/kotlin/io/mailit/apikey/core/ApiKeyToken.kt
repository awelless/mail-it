package io.mailit.apikey.core

import io.mailit.apikey.api.InvalidApiKeyException

@JvmInline
internal value class ApiKeyToken(val value: String) {

    fun decode(): Result<Pair<String, RawSecret>> {
        val match = patternRegex.matchEntire(value) ?: return Result.failure(InvalidApiKeyException())
        val (id, secret) = match.destructured
        return Result.success(id to RawSecret(secret))
    }

    companion object {
        private val patternRegex = Regex("^mailit_(?<id>[a-zA-Z0-9-#]+)_(?<secret>[a-zA-Z0-9-#]+)$")

        fun encode(id: String, secret: RawSecret) = ApiKeyToken("mailit_${id}_${secret.value}")
    }
}

@JvmInline
internal value class RawSecret(val value: String)
