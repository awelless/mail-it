package it.mail.admin.client.reflection

import io.quarkus.runtime.annotations.RegisterForReflection
import it.mail.core.model.Slice

@RegisterForReflection(targets = [Slice::class])
class DomainModelsReflectionConfig
