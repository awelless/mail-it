package io.mailit.persistence.test

import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.test.nowWithoutNanos
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
        expiresAt = nowWithoutNanos().plus(30.days.toJavaDuration()),
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
    fun findAll() = runTest {
        // given
        val apiKey2 = ApiKey(
            id = "id2",
            name = "Another Api Key",
            secret = "s3cr3t",
            application = application,
            expiresAt = nowWithoutNanos().plus(30.days.toJavaDuration()),
        )
        apiKeyRepository.create(apiKey2)

        // when
        val actual = apiKeyRepository.findAll(application.id)

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
            expiresAt = nowWithoutNanos().plus(30.days.toJavaDuration()),
        )

        // when
        apiKeyRepository.create(newApiKey)

        val actual = apiKeyRepository.findById(newApiKey.id)

        // then
        assertEquals(newApiKey, actual)
    }

    @Test
    fun delete() = runTest {
        // when
        apiKeyRepository.delete(apiKey.id)

        val actual = apiKeyRepository.findById(apiKey.id)

        // then
        assertNull(actual)
    }
}
