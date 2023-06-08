package io.mailit.core.admin.api.application

import io.mailit.core.model.Application
import io.mailit.core.model.Slice

interface ApplicationService {

    suspend fun getById(id: Long): Application

    suspend fun getAllSliced(page: Int, size: Int): Slice<Application>

    suspend fun create(command: CreateApplicationCommand): Application

    suspend fun delete(id: Long)
}

data class CreateApplicationCommand(
    val name: String,
)
