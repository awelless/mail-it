package it.mail.connector.http

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class IdDto(val id: Long)
