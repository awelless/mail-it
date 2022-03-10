package it.mail.web.external

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageRepository
import it.mail.repository.MailMessageTypeRepository
import it.mail.web.dto.CreateMailDto
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.BAD_REQUEST
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class ExternalMailResourceTest {

    val baseUrl = "/api/external/mail"

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository
    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        val typeName = "DEFAULT"
        mailMessageTypeRepository.persist(MailMessageType(typeName))
        mailType = mailMessageTypeRepository.findOneByName(typeName)!!
    }

    @AfterEach
    fun tearDown() {
        // TODO implement automated db cleanup
        mailMessageRepository.deleteAll()
        mailMessageTypeRepository.deleteAll()
    }

    @Test
    fun `with valid message - saves mail to db`() {
        val createMailDto = CreateMailDto(
            text = "Hello. How are you?",
            subject = "Greeting",
            from = "yoshito@gmail.com",
            to = "makise@gmail.com",
            type = mailType.name,
        )

        val messageId: String = Given {
            contentType(JSON)
            body(createMailDto)
        } When {
            post(baseUrl)
        } Then {
            statusCode(ACCEPTED)
        } Extract {
            path("id")
        }

        val savedMail = mailMessageRepository.findOneByExternalId(messageId)!!
        assertEquals(createMailDto.text, savedMail.text)
        assertEquals(createMailDto.subject, savedMail.subject)
        assertEquals(createMailDto.from, savedMail.emailFrom)
        assertEquals(createMailDto.to, savedMail.emailTo)
        assertEquals(mailType, savedMail.type)
    }

    @Test
    fun `without required fields - returns 400`() {
        Given {
            contentType(JSON)
            body(
                """
                    {
                        "subject":"a"
                    }"""
            )
        } When {
            post(baseUrl)
        } Then {
            statusCode(BAD_REQUEST)

            body("size()", equalTo(4))
            body("fieldName", hasItems("text", "from", "to", "type"))
            body("errorMessage", hasItems("Text shouldn't be blank", "Email from shouldn't be blank", "Email to shouldn't be blank"))
        }
    }
}
