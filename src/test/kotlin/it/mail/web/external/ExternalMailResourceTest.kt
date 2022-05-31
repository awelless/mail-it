package it.mail.web.external

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.core.model.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.test.createPlainMailMessageType
import it.mail.web.dto.CreateMailDto
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.BAD_REQUEST
import org.junit.jupiter.api.Assertions.assertEquals
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
        runBlocking {
            mailType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailType)
        }
    }

    @Test
    fun `sendMail with valid message - saves mail to db`() = runTest {
        val createMailDto = CreateMailDto(
            text = "Hello. How are you?",
            subject = "Greeting",
            from = "yoshito@gmail.com",
            to = "makise@gmail.com",
            typeId = mailType.id,
        )

        val messageId: Int = Given {
            contentType(JSON)
            body(createMailDto)
        } When {
            post(baseUrl)
        } Then {
            statusCode(ACCEPTED)
        } Extract {
            path("id")
        }

        val savedMail = mailMessageRepository.findOneWithTypeById(messageId.toLong())!!
        assertEquals(createMailDto.text, savedMail.text)
        assertEquals(createMailDto.subject, savedMail.subject)
        assertEquals(createMailDto.from, savedMail.emailFrom)
        assertEquals(createMailDto.to, savedMail.emailTo)
        assertEquals(mailType, savedMail.type)
    }

    @Test
    fun `sendMail without required fields - returns 400`() {
        Given {
            contentType(JSON)
            body(
                """
                    {
                        "subject":"a"
                    }
                """
            )
        } When {
            post(baseUrl)
        } Then {
            statusCode(BAD_REQUEST)
        }
    }

    @Test
    fun `sendMail with invalid message type - returns 400`() {
        val createMailDto = CreateMailDto(
            text = "Hello. How are you?",
            subject = "Greeting",
            from = "yoshito@gmail.com",
            to = "makise@gmail.com",
            typeId = 999999,
        )

        Given {
            contentType(JSON)
            body(createMailDto)
        } When {
            post(baseUrl)
        } Then {
            statusCode(BAD_REQUEST)
        } Extract {
            assertEquals("Invalid type: ${createMailDto.typeId} is passed", body().asString())
        }
    }
}
