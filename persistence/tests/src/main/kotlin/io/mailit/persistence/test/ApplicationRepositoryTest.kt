package io.mailit.persistence.test

import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.DELETED
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.spi.application.ApplicationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class ApplicationRepositoryTest {

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    lateinit var application: Application

    @BeforeEach
    fun setUp() {
        runBlocking {
            application = Application(
                id = 1,
                name = "Test Application",
                state = ENABLED,
            )
            applicationRepository.create(application)
        }
    }

    @Test
    fun `findById - returns when exists`() = runTest {
        // when
        val actual = applicationRepository.findById(application.id)

        // then
        assertEquals(application, actual)
    }

    @Test
    fun `findById - reutnrs null when doesn't exist`() = runTest {
        // when
        val actual = applicationRepository.findById(333)

        // then
        assertNull(actual)
    }

    @Test
    fun findAllSliced() = runTest {
        // given
        val application2 = Application(
            id = 2,
            name = "Another Test Application",
            state = ENABLED,
        )
        applicationRepository.create(application2)

        // when
        val actual = applicationRepository.findAllSliced(page = 1, size = 1)

        // then
        assertEquals(listOf(application), actual.content)
        assertTrue(actual.last)
    }

    @Test
    fun create() = runTest {
        // given
        val newApplication = Application(
            id = 123,
            name = "New Application",
            state = ENABLED,
        )

        // when
        applicationRepository.create(newApplication)

        val actual = applicationRepository.findById(newApplication.id)

        // then
        assertEquals(newApplication, actual)
    }

    @Test
    fun `updateState - marks as deleted`() = runTest {
        // when
        applicationRepository.updateState(
            id = application.id,
            state = DELETED,
        )

        val actual = applicationRepository.findById(application.id)

        // then
        assertEquals(DELETED, actual?.state)
    }
}
