package it.mail.web.admin

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageTypeRepository
import it.mail.web.DEFAULT_PAGE
import it.mail.web.DEFAULT_SIZE
import it.mail.web.PAGE_PARAM
import it.mail.web.SIZE_PARAM
import it.mail.web.dto.MailMessageTypeCreateDto
import it.mail.web.dto.MailMessageTypeUpdateDto
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class MailMessageTypeResourceTest {

    private val mailTypesUrl = "/api/admin/mail/type"
    private val mailTypeUrl = "$mailTypesUrl/{id}"

    private val idPath = "id"
    private val namePath = "name"
    private val descriptionPath = "description"
    private val maxRetriesCountPath = "maxRetriesCount"

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = MailMessageType("DEFAULT", "desc", 4)
        mailMessageTypeRepository.persist(mailType)
    }

    @Test
    fun getById() {
        val jsonPath = When {
            get(mailTypeUrl, mailType.id!!)
        } Then {
            statusCode(OK)
        } Extract {
            jsonPath()
        }

        assertEquals(mailType.id, jsonPath.getLong(idPath))
        assertEquals(mailType.name, jsonPath.getString(namePath))
        assertEquals(mailType.description, jsonPath.getString(descriptionPath))
        assertEquals(mailType.maxRetriesCount, jsonPath.getInt(maxRetriesCountPath))
    }

    @Test
    fun getAllSliced() {
        val jsonPath = Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 20)
        } When {
            get(mailTypesUrl)
        } Then {
            statusCode(OK)
        } Extract {
            jsonPath()
        }

        assertEquals(1, jsonPath.getInt("content.size()"))
        assertEquals(mailType.id, jsonPath.getLong("content[0].$idPath"))
        assertEquals(mailType.name, jsonPath.getString("content[0].$namePath"))
        assertEquals(mailType.description, jsonPath.getString("content[0].$descriptionPath"))
        assertEquals(mailType.maxRetriesCount, jsonPath.getInt("content[0].$maxRetriesCountPath"))
        assertEquals(0, jsonPath.getInt("page"))
        assertEquals(20, jsonPath.getInt("size"))
    }

    @Test
    fun `getAllSliced with no page and size - uses default`() {
        val jsonPath = When {
            get(mailTypesUrl)
        } Then {
            statusCode(OK)
        } Extract {
            jsonPath()
        }

        assertEquals(1, jsonPath.getInt("content.size()"))
        assertEquals(mailType.id, jsonPath.getLong("content[0].$idPath"))
        assertEquals(mailType.name, jsonPath.getString("content[0].$namePath"))
        assertEquals(mailType.description, jsonPath.getString("content[0].$descriptionPath"))
        assertEquals(mailType.maxRetriesCount, jsonPath.getInt("content[0].$maxRetriesCountPath"))
        assertEquals(DEFAULT_PAGE, jsonPath.getInt("page"))
        assertEquals(DEFAULT_SIZE, jsonPath.getInt("size"))
    }

    @Test
    fun create() {
        val createDto = MailMessageTypeCreateDto(
                name = "name",
                description = "yup",
                maxRetriesCount = 1,
        )

        val jsonPath = Given {
            contentType(JSON)
            body(createDto)
        } When {
            post(mailTypesUrl)
        } Then {
            statusCode(CREATED)
        } Extract {
            jsonPath()
        }

        assertEquals(createDto.name, jsonPath.getString(namePath))
        assertEquals(createDto.description, jsonPath.getString(descriptionPath))
        assertEquals(createDto.maxRetriesCount, jsonPath.getInt(maxRetriesCountPath))
    }

    @Test
    fun update() {
        val updateDto = MailMessageTypeUpdateDto(
                description = "yup",
                maxRetriesCount = 1,
        )

        val jsonPath = Given {
            contentType(JSON)
            body(updateDto)
        } When {
            put(mailTypeUrl, mailType.id)
        } Then {
            statusCode(OK)
        } Extract {
            jsonPath()
        }

        assertEquals(updateDto.description, jsonPath.getString(descriptionPath))
        assertEquals(updateDto.maxRetriesCount, jsonPath.getInt(maxRetriesCountPath))
    }
}