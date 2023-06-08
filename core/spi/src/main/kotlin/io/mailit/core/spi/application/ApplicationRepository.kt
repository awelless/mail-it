package io.mailit.core.spi.application

import io.mailit.core.exception.NotFoundException
import io.mailit.core.model.Slice
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState

interface ApplicationRepository {

    suspend fun findById(id: Long): Application?

    suspend fun findByIdOrThrow(id: Long) = findById(id) ?: throw NotFoundException("Application with id: $id is not found")

    suspend fun findAllSliced(page: Int, size: Int): Slice<Application>

    suspend fun create(application: Application)

    suspend fun updateState(id: Long, state: ApplicationState)
}
