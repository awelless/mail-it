package io.mailit.apikey.quarkus

import io.mailit.apikey.context.ApiKeyContext
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.apikey.spi.security.SecretHasher
import io.mailit.apikey.spi.security.SecureRandom
import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.inject.Singleton
import java.security.SecureRandom as JavaSecureRandom
import kotlin.streams.asSequence

class ApiKeyQuarkusContext(
    apiKeyRepository: ApiKeyRepository,
) {
    private val secretHasher = object : SecretHasher {
        override fun hash(rawSecret: String): String = BcryptUtil.bcryptHash(rawSecret)
        override fun matches(rawSecret: String, hashedSecret: String) = BcryptUtil.matches(rawSecret, hashedSecret)
    }

    private val secureRandom = object : SecureRandom {
        private val random = JavaSecureRandom.getInstance("SHA1PRNG")

        override fun generateInts(sequenceSize: Int, lowerBound: Int, upperBound: Int) =
            random.ints(sequenceSize.toLong(), lowerBound, upperBound).asSequence()
    }

    private val context = ApiKeyContext.create(apiKeyRepository, secretHasher, secureRandom)

    @Singleton
    fun apiKeyCrud() = context.apiKeyCrud

    @Singleton
    fun apiKeyValidator() = context.apiKeyValidator
}
