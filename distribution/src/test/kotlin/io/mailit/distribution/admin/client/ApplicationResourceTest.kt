package io.mailit.distribution.admin.client

import io.mailit.admin.console.http.DEFAULT_PAGE
import io.mailit.admin.console.http.DEFAULT_SIZE
import io.mailit.admin.console.http.PAGE_PARAM
import io.mailit.admin.console.http.SIZE_PARAM
import io.mailit.core.admin.api.application.CreateApplicationCommand
import io.mailit.core.model.Application
import io.mailit.core.model.ApplicationState.DELETED
import io.mailit.core.model.ApplicationState.ENABLED
import io.mailit.core.spi.ApplicationRepository
import io.mailit.test.restassured.body
import io.mailit.test.restassured.equalTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class ApplicationResourceTest {

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    lateinit var application: Application

    @BeforeEach
    fun setUp() {
        runBlocking {
            application = Application(
                id = 1,
                name = "Application",
                state = ENABLED,
            )

            applicationRepository.create(application)
        }
    }

    @Test
    fun getById() {
        When {
            get(APPLICATION_URL, application.id)
        } Then {
            statusCode(OK)

            body(
                ID equalTo application.id.toString(),
                NAME equalTo application.name,
                STATE equalTo application.state.name,
            )
        }
    }

    @Test
    fun getAllSliced() {
        Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 10)
        } When {
            get(APPLICATIONS_URL)
        } Then {
            statusCode(OK)

            body(
                "content.size()" equalTo 1,

                "content[0].$ID" equalTo application.id.toString(),
                "content[0].$NAME" equalTo application.name,
                "content[0].$STATE" equalTo application.state.name,

                "page" equalTo 0,
                "size" equalTo 10,

                "last" equalTo true,
            )
        }
    }

    @Test
    fun `getAllSliced with no page and size - uses default`() {
        When {
            get(APPLICATIONS_URL)
        } Then {
            statusCode(OK)
            body(
                "content.size()" equalTo 1,

                "content[0].$ID" equalTo application.id.toString(),
                "content[0].$NAME" equalTo application.name,
                "content[0].$STATE" equalTo application.state.name,

                "page" equalTo DEFAULT_PAGE,
                "size" equalTo DEFAULT_SIZE,

                "last" equalTo true,
            )
        }
    }

    @Test
    fun create() {
        val command = CreateApplicationCommand(
            name = "Another app",
        )

        Given {
            contentType(JSON)
            body(command)
        } When {
            post(APPLICATIONS_URL)
        } Then {
            statusCode(CREATED)

            body(
                NAME equalTo command.name,
                STATE equalTo ENABLED.name,
            )
        }
    }

    @Test
    fun delete() = runTest {
        When {
            delete(APPLICATION_URL, application.id)
        } Then {
            statusCode(NO_CONTENT)
        }

        val actual = applicationRepository.findById(application.id)

        assertEquals(DELETED, actual?.state)
    }

    companion object {
        private const val APPLICATIONS_URL = "/api/admin/applications"
        private const val APPLICATION_URL = "$APPLICATIONS_URL/{id}"

        private const val ID = "id"
        private const val NAME = "name"
        private const val STATE = "state"
    }
}
