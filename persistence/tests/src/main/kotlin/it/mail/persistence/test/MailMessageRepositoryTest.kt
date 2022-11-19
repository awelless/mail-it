package it.mail.persistence.test

import io.quarkus.test.junit.QuarkusTest
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus.PENDING
import it.mail.core.model.MailMessageStatus.SENDING
import it.mail.core.model.MailMessageType
import it.mail.core.persistence.api.MailMessageRepository
import it.mail.core.persistence.api.MailMessageTypeRepository
import it.mail.test.createPlainMailMessageType
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
open class MailMessageRepositoryTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailMessageRepository: MailMessageRepository

    lateinit var mailMessage: MailMessage
    lateinit var mailMessageType: MailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)
            mailMessageType = mailMessageTypeRepository.findByName(mailMessageType.name)!! // to get actual datetime values

            mailMessage = MailMessage(
                text = "text",
                data = emptyMap(),
                subject = null,
                emailFrom = "email@from.com",
                emailTo = "email@to.com",
                type = mailMessageType,
                createdAt = Instant.now(),
                status = PENDING,
            )
            val createdMailMessageType = mailMessageRepository.create(mailMessage)
            mailMessage = mailMessageRepository.findOneWithTypeById(createdMailMessageType.id)!! // to get actual datetime values
        }
    }

    @Test
    fun findOneWithTypeById_fetchesMailType() = runTest {
        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)!!

        assertEquals(mailMessage.id, actual.id)
        assertEquals(mailMessage.subject, actual.subject)
        assertEquals(mailMessage.emailFrom, actual.emailFrom)
        assertEquals(mailMessage.emailTo, actual.emailTo)
        assertEquals(mailMessage.createdAt, actual.createdAt)
        assertEquals(mailMessage.status, actual.status)

        // mailMessageType is fetched too
        assertEquals(mailMessageType.id, actual.type.id)
        assertEquals(mailMessageType.name, actual.type.name)
        assertEquals(mailMessageType.description, actual.type.description)
        assertEquals(mailMessageType.maxRetriesCount, actual.type.maxRetriesCount)
        assertEquals(mailMessageType.createdAt, actual.type.createdAt)
        assertEquals(mailMessageType.updatedAt, actual.type.updatedAt)
    }

    @Test
    fun findAllWithTypeByStatusesAndSendingStartedBefore_returns() = runTest {
        // given
        val messageSendingStartedAt = Instant.now().minusSeconds(10)

        val sendingMessage = MailMessage(
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = Instant.now().minusSeconds(100),
            sendingStartedAt = messageSendingStartedAt,
            status = SENDING,
        )
        mailMessageRepository.create(sendingMessage)

        // when
        val actual = mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(listOf(SENDING), Instant.now(), 1000)

        // then
        assertEquals(1, actual.size)
        assertEquals(sendingMessage.id, actual.first().id)
    }

    @Test
    fun findAllIdsByStatusIn_returnsIdsOnly() = runTest {
        // given
        val message2 = MailMessage(
            text = "text2",
            data = emptyMap(),
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            type = mailMessageType,
            createdAt = Instant.now(),
            status = PENDING,
        )
        mailMessageRepository.create(message2)

        // when
        val actual = mailMessageRepository.findAllIdsByStatusIn(listOf(PENDING), 1000)

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }
}
