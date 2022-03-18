package it.mail.repository

import io.quarkus.test.junit.QuarkusTest
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus.PENDING
import it.mail.domain.MailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID.randomUUID
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
        mailMessageType = MailMessageType("type")
        mailMessageTypeRepository.persist(mailMessageType)

        mailMessage = MailMessage(
            text = "text",
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            externalId = randomUUID().toString(),
            type = mailMessageType,
            createdAt = Instant.now(),
            status = PENDING,
        )
        mailMessageRepository.persist(mailMessage)
    }

    @Test
    fun findOneWithTypeByIdAndStatus_fetchesMailType() {
        val actual = mailMessageRepository.findOneWithTypeByIdAndStatus(mailMessage.id, listOf(PENDING))!!

        assertEquals(mailMessage.id, actual.id)
        assertEquals(mailMessage.subject, actual.subject)
        assertEquals(mailMessage.emailFrom, actual.emailFrom)
        assertEquals(mailMessage.emailTo, actual.emailTo)
        assertEquals(mailMessage.externalId, actual.externalId)
        assertEquals(mailMessage.createdAt.epochSecond, actual.createdAt.epochSecond)
        assertEquals(mailMessage.status, actual.status)

        // mailMessageType is fetched too
        assertEquals(mailMessageType.id, actual.type.id)
        assertEquals(mailMessageType.name, actual.type.name)
        assertEquals(mailMessageType.description, actual.type.description)
        assertEquals(mailMessageType.maxRetriesCount, actual.type.maxRetriesCount)
    }

    @Test
    fun findAllIdsByStatusIn_returnsIdsOnly() {
        // given
        val message2 = MailMessage(
            text = "text2",
            subject = null,
            emailFrom = "email@from.com",
            emailTo = "email@to.com",
            externalId = randomUUID().toString(),
            type = mailMessageType,
            createdAt = Instant.now(),
            status = PENDING,
        )
        mailMessageRepository.persist(message2)

        // when
        val actual = mailMessageRepository.findAllIdsByStatusIn(listOf(PENDING))

        // then
        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(mailMessage.id, message2.id)))
    }
}
