package io.mailit.core.model

data class Application(
    val id: Long,
    val name: String,
    val state: ApplicationState,
)

enum class ApplicationState {
    ENABLED,
    DELETED,
}
