package io.mailit.core.service.quarkus.application

import io.mailit.core.service.SecretHasher
import io.quarkus.elytron.security.common.BcryptUtil

object BCryptSecretHasher : SecretHasher {

    override fun hash(raw: String): String = BcryptUtil.bcryptHash(raw)

    override fun matches(raw: String, hashed: String) = BcryptUtil.matches(raw, hashed)
}
