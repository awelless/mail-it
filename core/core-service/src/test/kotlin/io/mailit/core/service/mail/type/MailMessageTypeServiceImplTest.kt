package io.mailit.core.service.mail.type

import io.mailit.core.api.admin.type.CreateMailMessageTypeCommand
import io.mailit.core.api.admin.type.MailMessageContentType
import io.mailit.core.api.admin.type.UpdateMailMessageTypeCommand
import io.mailit.core.model.MailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.test.createPlainMailMessageType
import io.mailit.value.exception.DuplicateUniqueKeyException
import io.mailit.value.exception.ValidationException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MailMessageTypeServiceImplTest {

    @MockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @MockK
    lateinit var mailMessageTypeFactory: MailMessageTypeFactory<MailMessageType>

    @RelaxedMockK
    lateinit var mailMessageTypeStateUpdater: MailMessageTypeStateUpdater<MailMessageType>

    @InjectMockKs
    lateinit var mailMessageTypeService: MailMessageTypeServiceImpl

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createPlainMailMessageType()
    }

    @Nested
    inner class CreateNewMailType {

        @Test
        fun `when everything is valid - creates`() = runTest {
            // given
            val command = CreateMailMessageTypeCommand(
                name = "NEW_TYPE",
                description = "some desc",
                maxRetriesCount = 11,
                contentType = MailMessageContentType.PLAIN_TEXT,
            )

            val mailType = createPlainMailMessageType()

            every { mailMessageTypeFactory.create(command) } returns mailType
            coEvery { mailMessageTypeRepository.create(mailType) } returnsArgument 0

            // when
            val actual = mailMessageTypeService.createNewMailType(command)

            // then
            assertSame(mailType, actual)
        }

        @Test
        fun `with non unique name - throws exception`() = runTest {
            val command = CreateMailMessageTypeCommand(
                name = "non unique",
                contentType = MailMessageContentType.PLAIN_TEXT,
            )

            every { mailMessageTypeFactory.create(command) } returns mailType
            coEvery { mailMessageTypeRepository.create(mailType) } throws DuplicateUniqueKeyException(null, null)

            assertThrows<ValidationException> { mailMessageTypeService.createNewMailType(command) }
        }
    }

    @Nested
    inner class UpdateMailType {

        @Test
        fun `when everything is valid - updates`() = runTest {
            // given
            val command = UpdateMailMessageTypeCommand(
                id = mailType.id,
                description = "new d",
                maxRetriesCount = 25,
            )

            val updatedMailType = createPlainMailMessageType()

            coEvery { mailMessageTypeRepository.findById(mailType.id) }.returns(mailType)
            coEvery { mailMessageTypeRepository.update(mailType) }.returns(updatedMailType)

            // when
            val actual = mailMessageTypeService.updateMailType(command)

            // then
            assertSame(updatedMailType, actual)

            verify(exactly = 1) { mailMessageTypeStateUpdater.update(mailType, command) }
        }
    }
}
