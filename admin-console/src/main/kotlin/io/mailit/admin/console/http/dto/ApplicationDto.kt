package io.mailit.admin.console.http.dto

import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ApplicationDto(
    val id: String,
    val name: String,
    val state: ApplicationState,
)

fun Application.toDto() = ApplicationDto(
    id = id.toString(),
    name = name,
    state = state,
)
