package it.mail.distribution.admin.client

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.admin.client.http.DEFAULT_PAGE
import it.mail.admin.client.http.DEFAULT_SIZE
import it.mail.admin.client.http.PAGE_PARAM
import it.mail.admin.client.http.SIZE_PARAM
import it.mail.admin.client.http.dto.MailMessageTypeCreateDto
import it.mail.admin.client.http.dto.MailMessageTypeUpdateDto
import it.mail.admin.client.security.UserCredentials
import it.mail.core.admin.api.type.MailMessageContentType
import it.mail.core.model.MailMessageType
import it.mail.core.model.MailMessageTypeState.DELETED
import it.mail.core.model.MailMessageTypeState.FORCE_DELETED
import it.mail.core.spi.MailMessageTypeRepository
import it.mail.test.createPlainMailMessageType
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class MailMessageTypeResourceTest {

    private val mailTypesUrl = "/api/admin/mails/types"
    private val mailTypeUrl = "$mailTypesUrl/{id}"
    private val mailTypeForceDeleteUrl = "$mailTypeUrl/force"

    private val idPath = "id"
    private val namePath = "name"
    private val descriptionPath = "description"
    private val maxRetriesCountPath = "maxRetriesCount"

    @Inject
    lateinit var userCredentials: UserCredentials
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
        Given {
            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            get(mailTypeUrl, mailType.id)
        } Then {
            statusCode(OK)

            body(
                idPath, equalTo(mailType.id.toInt()),
                namePath, equalTo(mailType.name),
                descriptionPath, equalTo(mailType.description),
                maxRetriesCountPath, equalTo(mailType.maxRetriesCount),
            )
        }
    }

    @Test
    fun getAllSliced() {
        Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 10)

            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            get(mailTypesUrl)
        } Then {
            statusCode(OK)

            body(
                "content.size()", equalTo(1),

                "content[0].$idPath", equalTo(mailType.id.toInt()),
                "content[0].$namePath", equalTo(mailType.name),
                "content[0].$descriptionPath", equalTo(mailType.description),
                "content[0].$maxRetriesCountPath", equalTo(mailType.maxRetriesCount),

                "page", equalTo(0),
                "size", equalTo(10),

                "last", equalTo(true),
            )
        }
    }

    @Test
    fun `getAllSliced with no page and size - uses default`() {
        Given {
            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            get(mailTypesUrl)
        } Then {
            statusCode(OK)
            body(
                "content.size()", equalTo(1),

                "content[0].$idPath", equalTo(mailType.id.toInt()),
                "content[0].$namePath", equalTo(mailType.name),
                "content[0].$descriptionPath", equalTo(mailType.description),
                "content[0].$maxRetriesCountPath", equalTo(mailType.maxRetriesCount),

                "page", equalTo(DEFAULT_PAGE),
                "size", equalTo(DEFAULT_SIZE),

                "last", equalTo(true),
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

            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            post(mailTypesUrl)
        } Then {
            statusCode(CREATED)

            body(
                namePath, equalTo(createDto.name),
                descriptionPath, equalTo(createDto.description),
                maxRetriesCountPath, equalTo(createDto.maxRetriesCount),
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

            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            put(mailTypeUrl, mailType.id)
        } Then {
            statusCode(OK)

            body(
                descriptionPath, equalTo(updateDto.description),
                maxRetriesCountPath, equalTo(updateDto.maxRetriesCount),
            )
        }
    }

    @Test
    suspend fun delete_marksAsDeleted() {
        Given {
            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            delete(mailTypeUrl, mailType.id)
        } Then {
            statusCode(ACCEPTED)
        }

        val actual = mailMessageTypeRepository.findById(mailType.id)!!

        assertEquals(DELETED, actual.state)
    }

    @Test
    suspend fun delete_force_marksAsForceDeleted() {
        Given {
            auth().preemptive().basic(userCredentials.username, String(userCredentials.password))
        } When {
            delete(mailTypeForceDeleteUrl, mailType.id)
        } Then {
            statusCode(ACCEPTED)
        }

        val actual = mailMessageTypeRepository.findById(mailType.id)!!

        assertEquals(FORCE_DELETED, actual.state)
    }
}
