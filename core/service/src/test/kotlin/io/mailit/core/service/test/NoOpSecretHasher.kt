package io.mailit.core.service.test

import io.mailit.core.service.admin.application.SecretHasher

object NoOpSecretHasher : SecretHasher {
    override fun hash(raw: String) = raw
    override fun matches(raw: String, hashed: String) = raw == hashed
}
