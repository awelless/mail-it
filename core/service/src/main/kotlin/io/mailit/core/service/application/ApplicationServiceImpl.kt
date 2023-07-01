package io.mailit.core.service.application

import io.mailit.core.admin.api.application.ApplicationService
import io.mailit.core.admin.api.application.CreateApplicationCommand
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState.DELETED
import io.mailit.core.model.application.ApplicationState.ENABLED
import io.mailit.core.service.id.IdGenerator
import io.mailit.core.spi.DuplicateUniqueKeyException
import io.mailit.core.spi.application.ApplicationRepository
import mu.KLogging

class ApplicationServiceImpl(
    private val applicationRepository: ApplicationRepository,
    private val idGenerator: IdGenerator,
) : ApplicationService {

    override suspend fun getById(id: Long) = applicationRepository.findByIdOrThrow(id)

    override suspend fun getAllSliced(page: Int, size: Int) = applicationRepository.findAllSliced(page, size)

    override suspend fun create(command: CreateApplicationCommand): Application {
        val application = Application(
            id = idGenerator.generateId(),
            name = command.name,
            state = ENABLED,
        )

        try {
            applicationRepository.create(application)
        } catch (e: DuplicateUniqueKeyException) {
            throw ValidationException("Application name: ${command.name} is not unique", e)
        }

        return application
    }

    override suspend fun delete(id: Long) {
        applicationRepository.updateState(id = id, state = DELETED)

        logger.info { "Application: $id is marked as deleted" }
    }

    companion object : KLogging()
}
