package io.mailit.core.spi

import io.mailit.core.model.Application
import io.mailit.core.model.ApplicationState
import io.mailit.core.model.Slice

interface ApplicationRepository {

    suspend fun findById(id: Long): Application?

    suspend fun findAllSliced(page: Int, size: Int): Slice<Application>

    suspend fun create(application: Application)

    suspend fun updateState(id: Long, state: ApplicationState)
}
