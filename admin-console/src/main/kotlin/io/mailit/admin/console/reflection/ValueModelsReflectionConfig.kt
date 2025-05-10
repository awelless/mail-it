package io.mailit.admin.console.reflection

import io.mailit.value.Slice
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection(targets = [Slice::class])
class ValueModelsReflectionConfig
