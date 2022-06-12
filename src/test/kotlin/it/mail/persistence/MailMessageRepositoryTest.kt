package it.mail.persistence

import io.quarkus.test.junit.QuarkusTest
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus.PENDING
import it.mail.core.model.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import javax.inject.Inject

@QuarkusTest
class MailMessageRepositoryTest {

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
            mailMessageRepository.create(mailMessage)
        }
    }

    @Test
    fun findOneWithTypeById_fetchesMailType() = runTest {
        val actual = mailMessageRepository.findOneWithTypeById(mailMessage.id)!!

        assertEquals(mailMessage.id, actual.id)
        assertEquals(mailMessage.subject, actual.subject)
        assertEquals(mailMessage.emailFrom, actual.emailFrom)
        assertEquals(mailMessage.emailTo, actual.emailTo)
        assertEquals(mailMessage.createdAt.epochSecond, actual.createdAt.epochSecond)
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
        val actual = mailMessageRepository.findAllIdsByStatusIn(listOf(PENDING))

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }
}
