package io.mailit.persistence.test

import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.test.minus
import io.mailit.test.nowWithoutNanos
import io.mailit.test.plus
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class ApiKeyRepositoryTest {

    @Inject
    lateinit var apiKeyRepository: ApiKeyRepository

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    private val application = Application(
        id = 1,
        name = "Test Application",
        state = ENABLED,
    )

    private val apiKey = ApiKey(
        id = "id",
        name = "api key",
        secret = "s3cr3t",
        application = application,
        createdAt = nowWithoutNanos() - 1.days,
        expiresAt = nowWithoutNanos() + 30.days,
    )

    @BeforeEach
    fun setUp() {
        runBlocking {
            applicationRepository.create(application)
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
    fun findAllByApplicationId() = runTest {
        // given
        val apiKey2 = ApiKey(
            id = "a",
            name = "Another Api Key",
            secret = "s3cr3t",
            application = application,
            createdAt = nowWithoutNanos(),
            expiresAt = nowWithoutNanos() + 30.days,
        ).also { apiKeyRepository.create(it) }

        val anotherApplication = Application(
            id = 2,
            name = "another",
            state = ENABLED,
        ).also { applicationRepository.create(it) }

        ApiKey(
            id = "another app id",
            name = "Another Api Key2222",
            secret = "s3cr3t",
            application = anotherApplication,
            createdAt = nowWithoutNanos(),
            expiresAt = nowWithoutNanos() + 30.days,
        ).also { apiKeyRepository.create(it) }

        // when
        val actual = apiKeyRepository.findAllByApplicationId(application.id)

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
            application = application,
            createdAt = nowWithoutNanos(),
            expiresAt = nowWithoutNanos() + 30.days,
        )

        // when
        apiKeyRepository.create(newApiKey)

        val actual = apiKeyRepository.findById(newApiKey.id)

        // then
        assertEquals(newApiKey, actual)
    }

    @Test
    fun `delete when exists for application`() = runTest {
        // when
        val deleted = apiKeyRepository.delete(application.id, apiKey.id)

        val actual = apiKeyRepository.findById(apiKey.id)

        // then
        assertTrue(deleted)
        assertNull(actual)
    }

    @Test
    fun `delete when doesn't exist for application`() = runTest {
        // when
        val deleted = apiKeyRepository.delete(
            applicationId = 9999,
            id = apiKey.id,
        )

        val actual = apiKeyRepository.findById(apiKey.id)

        // then
        assertFalse(deleted)
        assertNotNull(actual)
    }
}
