package io.mailit.persistence.test

import io.mailit.apikey.spi.persistence.ApiKey
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.test.minus
import io.mailit.test.nowWithoutNanos
import io.mailit.test.plus
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class ApiKeyRepositoryTest {

    @Inject
    lateinit var apiKeyRepository: ApiKeyRepository

    private val apiKey = ApiKey(
        id = "id",
        name = "api key",
        secret = "s3cr3t",
        createdAt = nowWithoutNanos() - 1.days,
        expiresAt = nowWithoutNanos() + 30.days,
    )

    @BeforeEach
    fun setUp() {
        runBlocking {
            apiKeyRepository.create(apiKey)
        }
    }

    @Test
    fun `findById - returns when exists`() = runTest {
        // when
        val actual = apiKeyRepository.findById(apiKey.id)

        // then
        assertEquals(apiKey, actual)
    }

    @Test
    fun `findById - reutnrs null when doesn't exist`() = runTest {
        // when
        val actual = apiKeyRepository.findById("invalid")

        // then
        assertNull(actual)
    }

    @Test
    fun findAll() = runTest {
        // given
        val apiKey2 = ApiKey(
            id = "a",
            name = "Another Api Key",
            secret = "s3cr3t",
            createdAt = nowWithoutNanos(),
            expiresAt = nowWithoutNanos() + 30.days,
        ).also { apiKeyRepository.create(it) }

        // when
        val actual = apiKeyRepository.findAll()

        // then
        assertEquals(listOf(apiKey2, apiKey), actual)
    }

    @Test
    fun create() = runTest {
        // given
        val newApiKey = ApiKey(
            id = "another",
            name = "Another Api Key",
            secret = "s3cr3t",
            createdAt = nowWithoutNanos(),
            expiresAt = nowWithoutNanos() + 30.days,
        )

        // when
        val result = apiKeyRepository.create(newApiKey)

        val actual = apiKeyRepository.findById(newApiKey.id)

        // then
        assertTrue(result.isSuccess)
        assertEquals(newApiKey, actual)
    }

    @Test
    fun `create with the same name`() = runTest {
        // given
        val newApiKey = apiKey.copy(id = "333")

        // when
        val result = apiKeyRepository.create(newApiKey)

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DuplicateUniqueKeyException)
    }

    @Test
    fun `delete when exists`() = runTest {
        // when
        val deleted = apiKeyRepository.delete(apiKey.id)

        val actual = apiKeyRepository.findById(apiKey.id)

        // then
        assertTrue(deleted)
        assertNull(actual)
    }
}
