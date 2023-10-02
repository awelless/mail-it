package io.mailit.admin.console.http.dto

import io.mailit.core.model.ApiKey
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant

@RegisterForReflection
data class ApiKeyDto(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)

@RegisterForReflection
data class ApiKeyTokenDto(val token: String)

data class CreateApiKeyDto(
    val name: String,
    val expirationDays: Int,
)

fun ApiKey.toDto() = ApiKeyDto(
    id = id,
    name = name,
    createdAt = createdAt,
    expiresAt = expiresAt,
)
