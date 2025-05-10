package io.mailit.distribution.admin.client

import io.mailit.admin.console.http.PAGE_PARAM
import io.mailit.admin.console.http.SIZE_PARAM
import io.mailit.admin.console.http.dto.MailMessageTypeCreateDto
import io.mailit.admin.console.http.dto.MailMessageTypeUpdateDto
import io.mailit.core.api.admin.type.MailMessageContentType
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
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
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class MailMessageTypeResourceTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() = runBlocking {
        mailType = createPlainMailMessageType()
        mailMessageTypeRepository.create(mailType)

        return@runBlocking
    }

    @Test
    fun getById() {
        When {
            get(MAIL_TYPE_URL, mailType.id.value)
        } Then {
            statusCode(OK)

            body(
                ID equalTo mailType.id.value.toString(),
                NAME equalTo mailType.name,
                DESCRIPTION equalTo mailType.description,
                MAX_RETRIES_COUNT equalTo mailType.maxRetriesCount,
            )
        }
    }

    @Test
    fun getAllSliced() {
        Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 10)
        } When {
            get(MAIL_TYPES_URL)
        } Then {
            statusCode(OK)

            body(
                "content.size()" equalTo 1,

                "content[0].$ID" equalTo mailType.id.value.toString(),
                "content[0].$NAME" equalTo mailType.name,
                "content[0].$DESCRIPTION" equalTo mailType.description,
                "content[0].$MAX_RETRIES_COUNT" equalTo mailType.maxRetriesCount,

                "page" equalTo 0,
                "size" equalTo 10,

                "last" equalTo true,
            )
        }
    }

    @Test
    fun `getAllSliced with no page and size - uses default`() {
        When {
            get(MAIL_TYPES_URL)
        } Then {
            statusCode(OK)
            body(
                "content.size()" equalTo 1,

                "content[0].$ID" equalTo mailType.id.value.toString(),
                "content[0].$NAME" equalTo mailType.name,
                "content[0].$DESCRIPTION" equalTo mailType.description,
                "content[0].$MAX_RETRIES_COUNT" equalTo mailType.maxRetriesCount,

                "page" equalTo DEFAULT_PAGE,
                "size" equalTo DEFAULT_SIZE,

                "last" equalTo true,
            )
        }
    }

    @Test
    fun create() {
        val createDto = MailMessageTypeCreateDto(
            name = "name",
            description = "yup",
            maxRetriesCount = 1,
            contentType = MailMessageContentType.PLAIN_TEXT,
        )

        Given {
            contentType(JSON)
            body(createDto)
        } When {
            post(MAIL_TYPES_URL)
        } Then {
            statusCode(CREATED)

            body(
                NAME equalTo createDto.name,
                DESCRIPTION equalTo createDto.description,
                MAX_RETRIES_COUNT equalTo createDto.maxRetriesCount,
            )
        }
    }

    @Test
    fun update() {
        val updateDto = MailMessageTypeUpdateDto(
            description = "yup",
            maxRetriesCount = 1,
        )

        Given {
            contentType(JSON)
            body(updateDto)
        } When {
            put(MAIL_TYPE_URL, mailType.id.value)
        } Then {
            statusCode(OK)

            body(
                DESCRIPTION equalTo updateDto.description,
                MAX_RETRIES_COUNT equalTo updateDto.maxRetriesCount,
            )
        }
    }

    @Test
    fun delete() = runTest {
        Given {
            param(FORCE_PARAM, "true")
        } When {
            delete(MAIL_TYPE_URL, mailType.id.value)
        } Then {
            statusCode(ACCEPTED)
        }

        val actual = mailMessageTypeRepository.findById(mailType.id)

        assertNull(actual)
    }

    @Test
    fun delete_force() = runTest {
        Given {
            param(FORCE_PARAM, "true")
        } When {
            delete(MAIL_TYPE_URL, mailType.id.value)
        } Then {
            statusCode(ACCEPTED)
        }

        val actual = mailMessageTypeRepository.findById(mailType.id)

        assertNull(actual)
    }

    companion object {
        private const val MAIL_TYPES_URL = "/api/admin/mails/types"
        private const val MAIL_TYPE_URL = "$MAIL_TYPES_URL/{id}"

        private const val FORCE_PARAM = "force"

        private const val ID = "id"
        private const val NAME = "name"
        private const val DESCRIPTION = "description"
        private const val MAX_RETRIES_COUNT = "maxRetriesCount"

        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 10
    }
}
