package io.mailit.connector.http

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class IdDto(val id: String)
