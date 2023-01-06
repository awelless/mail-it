package it.mail.admin.client.http.dto

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class IdNameDto(val id: String, val name: String)
