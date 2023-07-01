package io.mailit.core.service.application

import io.mailit.core.admin.api.application.CreateApplicationCommand
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.service.test.ConstantIdGenerator
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.application.ApplicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ApplicationServiceImplTest {

    @RelaxedMockK
    lateinit var applicationRepository: ApplicationRepository

    @SpyK
    var idGenerator = ConstantIdGenerator

    @InjectMockKs
    lateinit var applicationService: ApplicationServiceImpl

    @Test
    fun `when everything is valid - creates`() = runTest {
        // given
        val command = CreateApplicationCommand(
            name = "some_name",
        )

        val application = Application(
            id = ConstantIdGenerator.ID,
            name = command.name,
            state = ENABLED,
        )

        // when
        val actual = applicationService.create(command)

        // then
        assertEquals(application, actual)

        coVerify(exactly = 1) { applicationRepository.create(application) }
    }

    @Test
    fun `with non unique name - throws exception`() = runTest {
        // given
        val command = CreateApplicationCommand(
            name = "non_unique",
        )

        val application = Application(
            id = ConstantIdGenerator.ID,
            name = command.name,
            state = ENABLED,
        )

        coEvery { applicationRepository.create(application) } throws DuplicateUniqueKeyException(null, null)

        // when + then
        assertThrows<ValidationException> { applicationService.create(command) }
    }
}
