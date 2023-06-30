package io.mailit.distribution.admin.client

import io.mailit.admin.console.security.UserCredentials
import io.mailit.core.admin.api.application.ApiKeyService
import io.mailit.core.admin.api.application.CreateApiKeyCommand
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.spi.application.ApplicationRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.authentication.FormAuthConfig
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.jboss.resteasy.reactive.RestResponse.StatusCode.UNAUTHORIZED
import org.junit.jupiter.api.Test

@QuarkusTest
class AdminConsoleSecurityTest {

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    @Inject
    lateinit var apiKeyService: ApiKeyService

    @Inject
    lateinit var userCredentials: UserCredentials

    private val authConfig = FormAuthConfig("/api/admin/login", "username", "password")

    @Test
    fun `get mail types with valid credentials`() = runTest {
        Given {
            auth().form(userCredentials.username, String(userCredentials.password), authConfig)
        } When {
            get(MAIL_TYPES_URL)
        } Then {
            statusCode(OK)
        }
    }

    @Test
    fun `get mail types with invalid credentials`() = runTest {
        Given {
            auth().form("invalid", "invalid", authConfig)
        } When {
            get(MAIL_TYPES_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    @Test
    fun `get mail types with api key`() = runTest {
        val application = Application(id = 1, name = "test", state = ENABLED).also { applicationRepository.create(it) }
        val apiKeyToken = apiKeyService.generate(CreateApiKeyCommand(application.id, name = "valid-api-key", expiration = 30.days))

        Given {
            header(API_KEY_HEADER, apiKeyToken.value)
        } When {
            get(MAIL_TYPES_URL)
        } Then {
            statusCode(UNAUTHORIZED)
        }
    }

    companion object {
        private const val MAIL_TYPES_URL = "/api/admin/mails/types"

        private const val API_KEY_HEADER = "X-Api-Key"
    }
}
