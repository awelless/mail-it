package io.mailit.admin.console.reflection

import io.mailit.apikey.api.ApiKey
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection(targets = [ApiKey::class])
class ApiModelsReflectionConfig
