package io.mailit.admin.client.http.dto

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class IdNameDto(val id: String, val name: String)
