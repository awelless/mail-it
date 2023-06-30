package io.mailit.distribution.connector.http

import io.mailit.admin.console.security.UserCredentials
import io.mailit.core.admin.api.application.ApiKeyService
import io.mailit.core.admin.api.application.CreateApiKeyCommand
import io.mailit.core.external.api.CreateMailCommand
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.application.ApiKeyToken
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.DELETED
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.core.spi.application.ApplicationRepository
import io.mailit.test.createPlainMailMessageType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.authentication.FormAuthConfig
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
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
    lateinit var applicationRepository: ApplicationRepository

    @Inject
    lateinit var apiKeyService: ApiKeyService

    @Inject
    lateinit var userCredentials: UserCredentials

    lateinit var mailType: MailMessageType
    lateinit var application: Application
    var apiKeyToken: ApiKeyToken = ApiKeyToken("")

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailType = createPlainMailMessageType().also { mailMessageTypeRepository.create(it) }
            application = Application(id = 1, name = "test", state = ENABLED).also { applicationRepository.create(it) }
            apiKeyToken = apiKeyService.generate(CreateApiKeyCommand(application.id, name = "valid-api-key", expiration = 30.days))
        }
    }

    @Test
    fun `sendMail with valid api key`() = runTest {
        Given {
            contentType(JSON)
            body(createCommand())
            header(API_KEY_HEADER, apiKeyToken.value)
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
            body(createCommand())
            header(API_KEY_HEADER, "invalid")
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    @Test
    fun `sendMail with expired api key`() = runTest {
        val expiredToken = apiKeyService.generate(CreateApiKeyCommand(application.id, name = "valid-api-key", expiration = Duration.ZERO))

        Given {
            contentType(JSON)
            body(createCommand())
            header(API_KEY_HEADER, expiredToken.value)
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    @Test
    fun `sendMail for deleted application`() = runTest {
        applicationRepository.updateState(application.id, DELETED)

        Given {
            contentType(JSON)
            body(createCommand())
            header(API_KEY_HEADER, apiKeyToken.value)
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
            body(createCommand())
            auth().form(userCredentials.username, String(userCredentials.password), FormAuthConfig("/api/admin/login", "username", "password"))
        } When {
            post(SEND_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    private fun createCommand() = CreateMailCommand(
        text = "Hello. How are you?",
        data = null,
        subject = "Greeting",
        emailFrom = "yoshito@gmail.com",
        emailTo = "makise@gmail.com",
        mailType = mailType.name,
    )

    companion object {
        private const val SEND_URL = "/api/connector/mail"

        private const val API_KEY_HEADER = "X-Api-Key"
    }
}
