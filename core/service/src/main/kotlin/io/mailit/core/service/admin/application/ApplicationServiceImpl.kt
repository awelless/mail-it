package io.mailit.core.service.admin.application

import io.mailit.core.admin.api.application.ApplicationService
import io.mailit.core.admin.api.application.CreateApplicationCommand
import io.mailit.core.exception.NotFoundException
import io.mailit.core.exception.ValidationException
import io.mailit.core.model.Application
import io.mailit.core.model.ApplicationState.DELETED
import io.mailit.core.model.ApplicationState.ENABLED
import io.mailit.core.service.id.IdGenerator
import io.mailit.core.spi.ApplicationRepository
import io.mailit.core.spi.DuplicateUniqueKeyException
import mu.KLogging

class ApplicationServiceImpl(
    private val applicationRepository: ApplicationRepository,
    private val idGenerator: IdGenerator,
) : ApplicationService {

    override suspend fun getById(id: Long) =
        applicationRepository.findById(id) ?: throw NotFoundException("Application with id: $id is not found")

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
