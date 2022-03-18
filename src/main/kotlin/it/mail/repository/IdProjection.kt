package it.mail.repository

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class IdProjection(val id: Long)
