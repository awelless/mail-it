package io.mailit.apikey.spi.security

interface SecretHasher {

    fun hash(rawSecret: String): String

    fun matches(rawSecret: String, hashedSecret: String): Boolean
}
