package io.mailit.admin.console.reflection

import io.mailit.core.model.Slice
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection(targets = [Slice::class])
class DomainModelsReflectionConfig
