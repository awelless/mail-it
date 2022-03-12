package it.mail.service.admin

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageTypeRepository
import it.mail.service.BadRequestException
import it.mail.test.createMailMessageType
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
        fun `when everything is valid - creates`() {
            val name = "NEW_TYPE"
            val description = "some desc"
            val retries = 11

            every { mailMessageTypeRepository.existsOneWithName(name) }.returns(false)
            every { mailMessageTypeRepository.persist(any<MailMessageType>()) }.returnsArgument(0)

            val actual = mailMessageTypeService.createNewMailType(name, description, retries)

            assertEquals(name, actual.name)
            assertEquals(description, actual.description)
            assertEquals(retries, actual.maxRetriesCount)
        }

        @Test
        fun `with non unique name - throws exception`() {
            val name = "non unique"

            every { mailMessageTypeRepository.existsOneWithName(name) }.returns(true)

            assertThrows<BadRequestException> { mailMessageTypeService.createNewMailType(name, null, null) }
        }
    }

    @Nested
    inner class UpdateMailType {

        @Test
        fun `when everything is valid - updates`() {
            val description = "new d"
            val retries = null

            every { mailMessageTypeRepository.findById(mailType.id!!) }.returns(mailType)
            every { mailMessageTypeRepository.persist(any<MailMessageType>()) }.returnsArgument(0)

            val actual = mailMessageTypeService.updateMailType(mailType.id!!, description, retries)

            assertEquals(mailType.name, actual.name)
            assertEquals(description, actual.description)
            assertEquals(retries, actual.maxRetriesCount)
        }
    }
}
