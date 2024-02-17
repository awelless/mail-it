package io.mailit.distribution.connector.http

import io.mailit.admin.console.security.UserCredentials
import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.CreateApiKeyCommand
import io.mailit.connector.http.web.CreateMailDto
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.authentication.FormAuthConfig
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.UNAUTHORIZED
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class HttpConnectorSecurityTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var apiKeyCrud: ApiKeyCrud

    @Inject
    lateinit var userCredentials: UserCredentials

    lateinit var mailType: MailMessageType
    var apiKeyToken = ""

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailType = createPlainMailMessageType().also { mailMessageTypeRepository.create(it) }
            apiKeyToken = apiKeyCrud.generate(CreateApiKeyCommand(name = "valid-api-key", expiration = 30.days)).getOrThrow()
        }
    }

    @Test
    fun `sendMail with valid api key`() = runTest {
        Given {
            contentType(JSON)
            body(createRequestDto())
            header(API_KEY_HEADER, apiKeyToken)
        } When {
            post(SEND_URL)
        } Then {
            statusCode(ACCEPTED)
        }
    }

    @Test
    fun `sendMail with invalid api key`() = runTest {
        Given {
            contentType(JSON)
            body(createRequestDto())
            header(API_KEY_HEADER, "invalid")
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    @Test
    fun `sendMail with expired api key`() = runTest {
        val command = CreateApiKeyCommand(
            name = "expired-api-key",
            expiration = (-10).seconds, // Negative duration, so the expiration will be in the past.
        )
        val expiredToken = apiKeyCrud.generate(command)

        Given {
            contentType(JSON)
            body(createRequestDto())
            header(API_KEY_HEADER, expiredToken)
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    @Test
    fun `sendMail as user`() = runTest {
        Given {
            contentType(JSON)
            body(createRequestDto())
            auth().form(userCredentials.username, String(userCredentials.password), FormAuthConfig("/api/admin/login", "username", "password"))
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    private fun createRequestDto() = CreateMailDto(
        text = "Hello. How are you?",
        data = null,
        subject = "Greeting",
        emailFrom = "yoshito@gmail.com",
        emailTo = "makise@gmail.com",
        mailType = mailType.name,
        deduplicationId = "dedup",
    )

    companion object {
        private const val SEND_URL = "/api/connector/mail"

        private const val API_KEY_HEADER = "X-Api-Key"
    }
}
