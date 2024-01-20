package io.mailit.distribution.admin.client

import io.mailit.admin.console.http.CreateApiKeyDto
import io.mailit.apikey.spi.persistence.ApiKey as PersistenceApiKey
import io.mailit.apikey.spi.persistence.ApiKeyRepository
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

    lateinit var apiKey1: PersistenceApiKey
    lateinit var apiKey2: PersistenceApiKey

    @BeforeEach
    fun setUp() {
        runBlocking {
            val now = nowWithoutNanos()

            apiKey1 = PersistenceApiKey(
                id = "111",
                name = "test api key",
                secret = "s3cr3t",
                createdAt = now - 1.days,
                expiresAt = nowWithoutNanos() + 30.days,
            ).also { apiKeyRepository.create(it) }

            apiKey2 = PersistenceApiKey(
                id = "112",
                name = "test api key 2",
                secret = "s3cr3t33",
                createdAt = now,
                expiresAt = nowWithoutNanos() + 30.days,
            ).also { apiKeyRepository.create(it) }
        }
    }

    @Test
    fun getAll() {
        When {
            get(API_KEYS_URL)
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
            post(API_KEYS_URL)
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
    }

    @Test
    fun delete() = runTest {
        When {
            delete(API_KEY_URL, apiKey1.id)
        } Then {
            statusCode(NO_CONTENT)
        }

        val actual = apiKeyRepository.findById(apiKey1.id)

        assertNull(actual)
    }

    companion object {
        private const val API_KEYS_URL = "/api/admin/api-keys"
        private const val API_KEY_URL = "$API_KEYS_URL/{id}"

        private const val ID = "id"
        private const val NAME = "name"
        private const val CREATED_AT = "createdAt"
        private const val EXPIRES_AT = "expiresAt"
        private const val TOKEN = "token"
    }
}
