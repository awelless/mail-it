package io.mailit.admin.console.http.dto

import io.mailit.core.model.ApiKey
import java.time.Instant

data class ApiKeyDto(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)

data class CreateApiKeyDto(
    val name: String,
    val expirationDays: Int,
)

data class ApiKeyTokenDto(val token: String)

fun ApiKey.toDto() = ApiKeyDto(
    id = id,
    name = name,
    createdAt = createdAt,
    expiresAt = expiresAt,
)
