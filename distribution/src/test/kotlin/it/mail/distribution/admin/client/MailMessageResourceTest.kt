package it.mail.distribution.admin.client

import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import it.mail.admin.client.http.PAGE_PARAM
import it.mail.admin.client.http.SIZE_PARAM
import it.mail.domain.model.MailMessage
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class MailMessageResourceTest {

    private val mailsUrl = "/api/admin/mails"

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository

    lateinit var mail1: MailMessage
    lateinit var mail2: MailMessage

    @BeforeEach
    fun setUp() = runBlocking {
        val mailType = createPlainMailMessageType()
        mailMessageTypeRepository.create(mailType)

        mail1 = createMailMessage(mailType)
        mailMessageRepository.create(mail1)

        mail2 = createMailMessage(mailType)
        mailMessageRepository.create(mail2)

        return@runBlocking
    }

    @Test
    fun getAllSliced() {
        Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 20)
        } When {
            get(mailsUrl)
        } Then {
            statusCode(OK)

            body(
                "content.size()", equalTo(2),

                "content[0].id", equalTo(mail2.id.toInt()),
                "content[0].emailFrom", equalTo(mail2.emailFrom),
                "content[0].emailTo", equalTo(mail2.emailTo),
                "content[0].type.id", equalTo(mail2.type.id.toInt()),
                "content[0].type.name", equalTo(mail2.type.name),
                "content[0].status", equalTo(mail2.status.name),
                "content[0].failedCount", equalTo(mail2.failedCount),

                "content[1].id", equalTo(mail1.id.toInt()),
                "content[1].emailFrom", equalTo(mail1.emailFrom),
                "content[1].emailTo", equalTo(mail1.emailTo),
                "content[1].type.id", equalTo(mail1.type.id.toInt()),
                "content[1].type.name", equalTo(mail1.type.name),
                "content[1].status", equalTo(mail1.status.name),
                "content[1].failedCount", equalTo(mail1.failedCount)
            )
        }
    }
}
