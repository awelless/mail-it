package io.mailit.persistence.test

import io.mailit.core.model.MailMessageTypeState.DELETED
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.createPlainMailMessageType
import io.mailit.test.nowWithoutNanos
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class MailMessageTypeRepositoryTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    lateinit var mailMessageType: PlainTextMailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)
        }
    }

    @Test
    fun findById_whenExists_returns() = runTest {
        // when
        val actual = mailMessageTypeRepository.findById(mailMessageType.id)

        // then
        assertEquals(mailMessageType, actual)
    }

    @Test
    fun findById_whenNotExists_returnsNull() = runTest {
        // when
        val actual = mailMessageTypeRepository.findById(333)

        // then
        assertNull(actual)
    }

    @Test
    fun findByName_whenExists_returns() = runTest {
        // when
        val actual = mailMessageTypeRepository.findByName(mailMessageType.name)

        // then
        assertEquals(mailMessageType, actual)
    }

    @Test
    fun findByName_whenNotExists_returnsNull() = runTest {
        // when
        val actual = mailMessageTypeRepository.findByName("invalid")

        // then
        assertNull(actual)
    }

    @Test
    fun findAllSliced() = runTest {
        // given
        val type2 = createHtmlMailMessageType()
        mailMessageTypeRepository.create(type2)

        // when
        val actual = mailMessageTypeRepository.findAllSliced(page = 1, size = 1)

        // then
        assertEquals(listOf(mailMessageType), actual.content)
        assertTrue(actual.last)
    }

    @Test
    fun create() = runTest {
        // given
        val newMailMessageType = createHtmlMailMessageType()

        // when
        mailMessageTypeRepository.create(newMailMessageType)

        val actual = mailMessageTypeRepository.findById(newMailMessageType.id)

        // then
        assertEquals(newMailMessageType, actual)
    }

    @Test
    fun update() = runTest {
        // given
        val updatedMailMessageType = mailMessageType.copy(
            description = "new description",
            maxRetriesCount = 10,
        )

        // when
        mailMessageTypeRepository.update(updatedMailMessageType)

        val actual = mailMessageTypeRepository.findById(updatedMailMessageType.id)

        // then
        assertEquals(updatedMailMessageType, actual)
    }

    @Test
    fun updateState_deletesType() = runTest {
        // when
        mailMessageTypeRepository.updateState(
            id = mailMessageType.id,
            state = DELETED,
            updatedAt = nowWithoutNanos(),
        )

        val actual = mailMessageTypeRepository.findById(mailMessageType.id)

        // then
        assertNull(actual)
    }
}
