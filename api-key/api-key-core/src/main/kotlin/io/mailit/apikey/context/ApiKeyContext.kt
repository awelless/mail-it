package io.mailit.apikey.context

import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.ApiKeyValidator
import io.mailit.apikey.core.ApiKeyCrudImpl
import io.mailit.apikey.core.ApiKeyFactory
import io.mailit.apikey.core.ApiKeyValidatorImpl
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.apikey.spi.security.SecretHasher
import io.mailit.apikey.spi.security.SecureRandom

class ApiKeyContext private constructor(
    val apiKeyCrud: ApiKeyCrud,
    val apiKeyValidator: ApiKeyValidator,
) {
    companion object {
        fun create(apiKeyRepository: ApiKeyRepository, secretHasher: SecretHasher, secureRandom: SecureRandom): ApiKeyContext {
            val apiKeyFactory = ApiKeyFactory(secureRandom, secretHasher)

            return ApiKeyContext(
                apiKeyCrud = ApiKeyCrudImpl(apiKeyFactory, apiKeyRepository),
                apiKeyValidator = ApiKeyValidatorImpl(apiKeyRepository, secretHasher),
            )
        }
    }
}
