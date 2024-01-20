package io.mailit.apikey.test

import io.mailit.apikey.api.ApiKeyValidator
import io.mailit.apikey.api.InvalidApiKeyException

class PassingApiKeyValidator(private val name: String) : ApiKeyValidator {
    override suspend fun validate(token: String) = Result.success(name)
}

object FailingApiKeyValidator : ApiKeyValidator {
    override suspend fun validate(token: String) = Result.failure<String>(InvalidApiKeyException())
}
