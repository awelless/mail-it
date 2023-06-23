package io.mailit.distribution.admin.client

import io.mailit.admin.console.http.dto.CreateApiKeyDto
import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.test.minus
import io.mailit.test.nowWithoutNanos
import io.mailit.test.plus
import io.mailit.test.restassured.Null
import io.mailit.test.restassured.body
import io.mailit.test.restassured.equalTo
import io.mailit.test.restassured.not
import io.mailit.test.within
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class ApiKeyResourceTest {

    @Inject
    lateinit var apiKeyRepository: ApiKeyRepository

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    lateinit var application: Application
    lateinit var apiKey1: ApiKey
    lateinit var apiKey2: ApiKey

    @BeforeEach
    fun setUp() {
        runBlocking {
            application = Application(
                id = 1,
                name = "Application",
                state = ApplicationState.ENABLED,
            ).also { applicationRepository.create(it) }

            val now = nowWithoutNanos()

            apiKey1 = ApiKey(
                id = "111",
                name = "test api key",
                secret = "s3cr3t",
                application = application,
                createdAt = now - 1.days,
                expiresAt = nowWithoutNanos() + 30.days,
            ).also { apiKeyRepository.create(it) }

            apiKey2 = ApiKey(
                id = "112",
                name = "test api key 2",
                secret = "s3cr3t33",
                application = application,
                createdAt = now,
                expiresAt = nowWithoutNanos() + 30.days,
            ).also { apiKeyRepository.create(it) }
        }
    }

    @Test
    fun getAll() {
        When {
            get(API_KEYS_URL, application.id)
        } Then {
            statusCode(OK)

            body(
                "size()" equalTo 2,

                "[0].$ID" equalTo apiKey2.id,
                "[0].$NAME" equalTo apiKey2.name,
                "[0].$CREATED_AT" not Null,
                "[0].$EXPIRES_AT" not Null,

                "[1].$ID" equalTo apiKey1.id,
                "[1].$NAME" equalTo apiKey1.name,
                "[1].$CREATED_AT" not Null,
                "[1].$EXPIRES_AT" not Null,
            )
        }
    }

    @Test
    fun generate() = runTest {
        val dto = CreateApiKeyDto(
            name = "api key name",
            expirationDays = 30,
        )

        val token: String = Given {
            contentType(JSON)
            body(dto)
        } When {
            post(API_KEYS_URL, application.id)
        } Then {
            statusCode(CREATED)

            body(TOKEN to Matchers.startsWith("mailit_"))
        } Extract {
            path(TOKEN)
        }

        val id = token.split("_")[1]
        val expectedExpiresAt = Instant.now() + dto.expirationDays.days

        val apiKey = apiKeyRepository.findById(id)

        assertTrue(expectedExpiresAt.within(1.seconds, of = apiKey!!.expiresAt))
        assertEquals(dto.name, apiKey.name)
        assertEquals(application, apiKey.application)
    }

    @Test
    fun delete() = runTest {
        When {
            delete(API_KEY_URL, application.id, apiKey1.id)
        } Then {
            statusCode(NO_CONTENT)
        }

        val actual = apiKeyRepository.findById(apiKey1.id)

        assertNull(actual)
    }

    companion object {
        private const val API_KEYS_URL = "/api/admin/applications/{applicationId}/api-keys"
        private const val API_KEY_URL = "$API_KEYS_URL/{id}"

        private const val ID = "id"
        private const val NAME = "name"
        private const val CREATED_AT = "createdAt"
        private const val EXPIRES_AT = "expiresAt"
        private const val TOKEN = "token"
    }
}
