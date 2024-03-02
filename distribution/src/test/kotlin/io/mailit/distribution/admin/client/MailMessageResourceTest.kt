package io.mailit.distribution.admin.client

import io.mailit.admin.console.http.PAGE_PARAM
import io.mailit.admin.console.http.SIZE_PARAM
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.test.nowWithoutNanos
import io.mailit.test.restassured.body
import io.mailit.test.restassured.equalTo
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.PersistenceMail
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.runBlocking
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class MailMessageResourceTest {

    private val counter = AtomicLong()

    private val mailsUrl = "/api/admin/mails"

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailRepository: MailRepository

    lateinit var mailType: MailMessageType
    lateinit var mail1: PersistenceMail
    lateinit var mail2: PersistenceMail

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailType)

            mail1 = createMail(mailType)
            mail2 = createMail(mailType)
        }
    }

    @Test
    fun getAllSliced() {
        Given {
            param(PAGE_PARAM, 0)
            param(SIZE_PARAM, 10)
        } When {
            get(mailsUrl)
        } Then {
            statusCode(OK)

            body(
                "content.size()" equalTo 2,

                "content[0].id" equalTo mail2.id.value.toString(),
                "content[0].emailFrom" equalTo mail2.emailFrom?.email,
                "content[0].emailTo" equalTo mail2.emailTo.email,
                "content[0].type.id" equalTo mailType.id.value.toString(),
                "content[0].type.name" equalTo mailType.name,
                "content[0].status" equalTo mail2.state.name,
                "content[0].failedCount" equalTo mail2.failedCount,

                "content[1].id" equalTo mail1.id.value.toString(),
                "content[1].emailFrom" equalTo mail1.emailFrom?.email,
                "content[1].emailTo" equalTo mail1.emailTo.email,
                "content[1].type.id" equalTo mailType.id.value.toString(),
                "content[1].type.name" equalTo mailType.name,
                "content[1].status" equalTo mail1.state.name,
                "content[1].failedCount" equalTo mail1.failedCount,

                "last" equalTo true,
            )
        }
    }

    private suspend fun createMail(messageType: MailMessageType): PersistenceMail {
        val id = counter.incrementAndGet()

        return PersistenceMail(
            id = MailId(id),
            mailTypeId = messageType.id,
            text = "text",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com".toEmailAddress(),
            emailTo = "email@to.com".toEmailAddress(),
            createdAt = nowWithoutNanos(),
            sendingStartedAt = null,
            sentAt = null,
            state = MailState.PENDING,
            failedCount = 0,
            deduplicationId = id.toString(),
        ).also { mailRepository.create(it) }
    }
}
