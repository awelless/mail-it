package io.mailit.distribution.admin.client

import io.mailit.admin.console.security.UserCredentials
import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.CreateApiKeyCommand
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
    lateinit var apiKeyCrud: ApiKeyCrud

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
        val apiKeyToken = apiKeyCrud.generate(CreateApiKeyCommand(name = "valid-api-key", expiration = 30.days)).getOrThrow()

        Given {
            header(API_KEY_HEADER, apiKeyToken)
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
