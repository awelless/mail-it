package io.mailit.admin.console.http.dto

import io.mailit.core.model.application.ApplicationState
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ApplicationDto(
    val id: String,
    val name: String,
    val state: ApplicationState,
)
