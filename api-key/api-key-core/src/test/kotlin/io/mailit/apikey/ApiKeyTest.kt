package io.mailit.apikey

import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.ApiKeyValidator
import io.mailit.apikey.api.CreateApiKeyCommand
import io.mailit.apikey.api.InvalidApiKeyException
import io.mailit.apikey.context.ApiKeyContext
import io.mailit.apikey.core.ApiKey
import io.mailit.apikey.core.ApiKeyToken
import io.mailit.apikey.core.RawSecret
import io.mailit.apikey.fake.ConstantSecuredRandom
import io.mailit.apikey.fake.InMemoryApiKeyRepository
import io.mailit.apikey.fake.NoOpSecretHasher
import io.mailit.apikey.lang.flatMap
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ApiKeyTest {

    private lateinit var apiKeyRepository: ApiKeyRepository
    private val secretHasher = NoOpSecretHasher

    private lateinit var apiKeyCrud: ApiKeyCrud
    private lateinit var apiKeyValidator: ApiKeyValidator

    @BeforeEach
    fun setUp() {
        apiKeyRepository = InMemoryApiKeyRepository()

        val context = ApiKeyContext.create(
            apiKeyRepository,
            secretHasher,
            ConstantSecuredRandom,
        )

        apiKeyCrud = context.apiKeyCrud
        apiKeyValidator = context.apiKeyValidator
    }

    @Nested
    inner class ApiKeyCrudTest {

        @Test
        fun generate() = runTest {
            // given
            val name = "apiKey"
            val command = CreateApiKeyCommand(
                name = name,
                expiration = 30.days,
            )

            // when
            val (tokenId, rawSecret) = apiKeyCrud.generate(command).flatMap { ApiKeyToken(it).decode() }.getOrThrow()
            val apiKeys = apiKeyRepository.findAll()
            val apiKey = ApiKey.fromPersistenceModel(apiKeys[0])

            // then
            assertEquals(1, apiKeys.size)
            assertEquals(tokenId, apiKey.id)
            assertEquals(name, apiKey.name)
            assertTrue(apiKey.matchesSecret(secretHasher, rawSecret))
        }
    }

    @Nested
    inner class ApiKeyValidatorTest {

        @Test
        fun `validate when token is valid`() = runTest {
            // given
            val name = "key"
            val token = createApiKey(name = name)

            // when
            val actual = apiKeyValidator.validate(token).getOrThrow()

            // then
            assertEquals(name, actual)
        }

        @Test
        fun `validate when token id is invalid`() = runTest {
            // given
            val token = createApiKey()
            val (_, secret) = ApiKeyToken(token).decode().getOrThrow()

            val invalidToken = ApiKeyToken.encode("invalid", secret)

            // when
            val actual = apiKeyValidator.validate(invalidToken.value)

            // then
            assertInvalidApiKeyException(actual)
        }

        @Test
        fun `validate when secret is invalid`() = runTest {
            // given
            val token = createApiKey()
            val (id, _) = ApiKeyToken(token).decode().getOrThrow()

            val invalidToken = ApiKeyToken.encode(id, RawSecret("invalid_secret"))

            // when
            val actual = apiKeyValidator.validate(invalidToken.value)

            // then
            assertInvalidApiKeyException(actual)
        }

        @Test
        fun `validate when api key is expired`() = runTest {
            // given
            val token = createApiKey(expiration = 1.nanoseconds)

            // when
            val actual = apiKeyValidator.validate(token)

            // then
            assertInvalidApiKeyException(actual)
        }

        private fun assertInvalidApiKeyException(actual: Result<*>) {
            val error = actual.exceptionOrNull()!!

            assertTrue(error is InvalidApiKeyException)
            assertEquals("API Key is invalid", error.message)
        }
    }

    private fun createApiKey(name: String = "api-key", expiration: Duration = 30.days) = runBlocking {
        apiKeyCrud.generate(CreateApiKeyCommand(name, expiration)).getOrThrow()
    }
}
