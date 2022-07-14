package it.mail.distribution.connector.http

import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.domain.external.api.CreateMailCommand
import it.mail.domain.model.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.BAD_REQUEST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class HttpConnectorTest {

    val baseUrl = "/api/connector/mail"

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
            mailType = mailMessageTypeRepository.findByName(mailType.name)!! // to get actual datetime values
        }
    }

    @Test
    fun `sendMail with valid message - saves mail to db`() = runTest {
        val createMailDto = CreateMailCommand(
            text = "Hello. How are you?",
            data = null,
            subject = "Greeting",
            emailFrom = "yoshito@gmail.com",
            emailTo = "makise@gmail.com",
            mailMessageTypeId = mailType.id,
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
        assertNull(savedMail.data)
        assertEquals(createMailDto.subject, savedMail.subject)
        assertEquals(createMailDto.emailFrom, savedMail.emailFrom)
        assertEquals(createMailDto.emailTo, savedMail.emailTo)
        assertEquals(mailType, savedMail.type)
    }

    @Test
    fun `sendMail with template data in message - saves mail to db`() = runTest {
        val createMailDto = CreateMailCommand(
            text = null,
            data = mapOf("oranges" to 2.39, "apples" to 0.99),
            subject = "Purchase receipt",
            emailFrom = "yoshito@gmail.com",
            emailTo = "makise@gmail.com",
            mailMessageTypeId = mailType.id,
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
        assertNull(savedMail.text)
        assertEquals(createMailDto.data, savedMail.data)
        assertEquals(createMailDto.subject, savedMail.subject)
        assertEquals(createMailDto.emailFrom, savedMail.emailFrom)
        assertEquals(createMailDto.emailTo, savedMail.emailTo)
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
        val createMailDto = CreateMailCommand(
            text = "Hello. How are you?",
            data = null,
            subject = "Greeting",
            emailFrom = "yoshito@gmail.com",
            emailTo = "makise@gmail.com",
            mailMessageTypeId = 999999,
        )

        Given {
            contentType(JSON)
            body(createMailDto)
        } When {
            post(baseUrl)
        } Then {
            statusCode(BAD_REQUEST)
        } Extract {
            assertEquals("Invalid type: ${createMailDto.mailMessageTypeId} is passed", body().asString())
        }
    }
}
