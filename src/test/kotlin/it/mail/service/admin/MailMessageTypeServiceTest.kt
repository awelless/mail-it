package it.mail.service.admin

import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import it.mail.domain.MailMessageType
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.ValidationException
import it.mail.test.createMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageTypeServiceTest {

    @MockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var mailMessageTypeService: MailMessageTypeService

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createMailMessageType()
    }

    @Nested
    inner class CreateNewMailType {

        @Test
        fun `when everything is valid - creates`() = runTest {
            val name = "NEW_TYPE"
            val description = "some desc"
            val retries = 11

            coEvery { mailMessageTypeRepository.existsOneWithName(name) }.returns(false)
            coEvery { mailMessageTypeRepository.persist(any<MailMessageType>()) }.returnsArgument(0)

            val actual = mailMessageTypeService.createNewMailType(name, description, retries)

            assertEquals(name, actual.name)
            assertEquals(description, actual.description)
            assertEquals(retries, actual.maxRetriesCount)
        }

        @Test
        fun `with non unique name - throws exception`() = runTest {
            val name = "non unique"

            coEvery { mailMessageTypeRepository.existsOneWithName(name) }.returns(true)

            assertThrows<ValidationException> { mailMessageTypeService.createNewMailType(name, null, null) }
        }
    }

    @Nested
    inner class UpdateMailType {

        @Test
        fun `when everything is valid - updates`() = runTest {
            val description = "new d"
            val retries = null

            coEvery { mailMessageTypeRepository.findById(mailType.id) }.returns(mailType)
            coEvery { mailMessageTypeRepository.persist(any()) }.returnsArgument(0)

            val actual = mailMessageTypeService.updateMailType(mailType.id, description, retries)

            assertEquals(mailType.name, actual.name)
            assertEquals(description, actual.description)
            assertEquals(retries, actual.maxRetriesCount)
        }
    }
}
