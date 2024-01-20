package io.mailit.apikey.core

import io.mailit.apikey.api.ApiKeyValidator
import io.mailit.apikey.api.InvalidApiKeyException
import io.mailit.apikey.lang.ensure
import io.mailit.apikey.lang.flatMap
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.apikey.spi.security.SecretHasher
import java.time.Instant
import mu.KLogging

internal class ApiKeyValidatorImpl(
    private val apiKeyRepository: ApiKeyRepository,
    private val secretHasher: SecretHasher,
) : ApiKeyValidator {

    override suspend fun validate(token: String) =
        ApiKeyToken(token).decode().flatMap { (id, rawSecret) ->
            findApiKey(id)
                .validateApiKey(rawSecret)
                .map { it.name }
        }

    private suspend fun findApiKey(id: String): Result<ApiKey> {
        val apiKey = apiKeyRepository.findById(id)
        return apiKey?.let { Result.success(ApiKey.fromPersistenceModel(it)) } ?: invalidApiKeyResult()
    }

    private fun Result<ApiKey>.validateApiKey(rawSecret: RawSecret) =
        ensure({ it.isNotExpired(Instant.now()) }) {
            logger.debug { "ApiKey: ${it.id} is expired" }
            InvalidApiKeyException()
        }
            .ensure({ it.matchesSecret(secretHasher, rawSecret) }) {
                logger.debug { "Invalid secret provided for ApiKey: ${it.id}" }
                InvalidApiKeyException()
            }

    private fun <T> invalidApiKeyResult() = Result.failure<T>(InvalidApiKeyException())

    companion object : KLogging()
}
