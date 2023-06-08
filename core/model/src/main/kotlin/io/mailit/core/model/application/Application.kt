package io.mailit.core.model.application

data class Application(
    val id: Long,
    val name: String,
    val state: ApplicationState,
)

enum class ApplicationState {
    ENABLED,
    DELETED,
}
