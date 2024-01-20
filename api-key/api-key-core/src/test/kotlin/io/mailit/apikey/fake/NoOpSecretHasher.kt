package io.mailit.apikey.fake

import io.mailit.apikey.spi.security.SecretHasher

object NoOpSecretHasher : SecretHasher {

    override fun hash(rawSecret: String) = "hashed$rawSecret"

    override fun matches(rawSecret: String, hashedSecret: String) = hash(rawSecret) == hashedSecret
}
