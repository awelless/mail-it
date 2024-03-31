package io.mailit.persistence.test

import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.worker.spi.persistence.MailTypeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class WorkerMailTypeRepositoryTest {

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @Inject
    lateinit var mailTypeRepository: MailTypeRepository

    lateinit var mailMessageType: PlainTextMailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createPlainMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)
        }
    }

    @Test
    fun `findActiveIdByName - when exists - returns id`() = runTest {
        // when
        val actual = mailTypeRepository.findActiveIdByName(mailMessageType.name)

        // then
        assertEquals(mailMessageType.id, actual)
    }

    @Test
    fun `findActiveIdByName - when doesn't exists - returns null`() = runTest {
        // when
        val actual = mailTypeRepository.findActiveIdByName("invalid")

        // then
        assertNull(actual)
    }
}
