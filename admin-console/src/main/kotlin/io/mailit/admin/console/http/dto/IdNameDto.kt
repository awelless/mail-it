package io.mailit.admin.console.http.dto

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class IdNameDto(val id: String, val name: String)
