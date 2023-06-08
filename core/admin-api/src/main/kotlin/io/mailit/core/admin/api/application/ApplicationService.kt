package io.mailit.core.admin.api.application

import io.mailit.core.model.Slice
import io.mailit.core.model.application.Application

interface ApplicationService {

    suspend fun getById(id: Long): Application

    suspend fun getAllSliced(page: Int, size: Int): Slice<Application>

    suspend fun create(command: CreateApplicationCommand): Application

    suspend fun delete(id: Long)
}

data class CreateApplicationCommand(
    val name: String,
)
