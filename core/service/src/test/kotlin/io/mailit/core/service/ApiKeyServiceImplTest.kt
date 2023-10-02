package io.mailit.core.service

import io.mailit.core.admin.api.CreateApiKeyCommand
import io.mailit.core.external.api.InvalidApiKeyException
import io.mailit.core.model.ApiKey
import io.mailit.core.model.ApiKeyToken
import io.mailit.core.service.test.NoOpSecretHasher
import io.mailit.core.spi.ApiKeyRepository
import io.mailit.test.within
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ApiKeyServiceImplTest {

    @MockK
    lateinit var apiKeyGenerator: ApiKeyGenerator

    @RelaxedMockK
    lateinit var apiKeyRepository: ApiKeyRepository

    @SpyK
    var secretHasher = NoOpSecretHasher

    @InjectMockKs
    lateinit var apiKeyService: ApiKeyServiceImpl

    private val apiKey = ApiKey(
        id = "id",
        name = "my api key",
        secret = "secret",
        createdAt = Instant.now(),
        expiresAt = Instant.now().plus(30.days.toJavaDuration()),
    )

    @Test
    fun generate() = runTest {
        // given
        val command = CreateApiKeyCommand(
            name = apiKey.name,
            expiration = 30.days,
        )
        val token = ApiKeyToken("token")

        coEvery { apiKeyGenerator.generateId() } returns apiKey.id
        coEvery { apiKeyGenerator.generateSecret() } returns apiKey.secret
        every { ApiKeyTokenCodec.encode(apiKey.id, apiKey.secret) } returns token

        // when
        val actual = apiKeyService.generate(command)

        // then
        assertEquals(token, actual)

        coVerify(exactly = 1) {
            apiKeyRepository.create(
                match {
                    it.id == apiKey.id &&
                        it.name == apiKey.name &&
                        it.secret == apiKey.secret &&
                        it.expiresAt.within(1.seconds, of = Instant.now().plus(command.expiration.toJavaDuration()))
                },
            )
        }
    }

    @Test
    fun `validate when token is valid`() = runTest {
        // given
        val token = ApiKeyToken("token")

        every { ApiKeyTokenCodec.decode(token) } returns (apiKey.id to apiKey.secret)
        coEvery { apiKeyRepository.findById(apiKey.id) } returns apiKey

        // when
        val actual = apiKeyService.validate(token)

        // then
        assertEquals(apiKey.name, actual)
    }

    @Test
    fun `validate when token id is invalid`() = runTest {
        // given
        val token = ApiKeyToken("token")

        every { ApiKeyTokenCodec.decode(token) } returns (apiKey.id to apiKey.secret)
        coEvery { apiKeyRepository.findById(apiKey.id) } returns null

        // when + then
        assertThrows<InvalidApiKeyException> { apiKeyService.validate(token) }
    }

    @Test
    fun `validate when secret is invalid`() = runTest {
        // given
        val token = ApiKeyToken("token")

        every { ApiKeyTokenCodec.decode(token) } returns (apiKey.id to "invalid secret")
        coEvery { apiKeyRepository.findById(apiKey.id) } returns apiKey

        // when + then
        assertThrows<InvalidApiKeyException> { apiKeyService.validate(token) }
    }

    @Test
    fun `validate when api key is expired`() = runTest {
        // given
        val expiredKey = ApiKey(
            id = "expired",
            name = "expired",
            secret = "123",
            createdAt = Instant.now(),
            expiresAt = Instant.now().minus(1.days.toJavaDuration()),
        )
        val token = ApiKeyToken("token")

        every { ApiKeyTokenCodec.decode(token) } returns (expiredKey.id to expiredKey.secret)
        coEvery { apiKeyRepository.findById(expiredKey.id) } returns expiredKey

        // when + then
        assertThrows<InvalidApiKeyException> { apiKeyService.validate(token) }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockkObject(ApiKeyTokenCodec)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            unmockkObject(ApiKeyTokenCodec)
        }
    }
}
