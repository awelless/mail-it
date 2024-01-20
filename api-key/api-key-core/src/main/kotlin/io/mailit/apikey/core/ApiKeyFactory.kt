package io.mailit.apikey.core

import io.mailit.apikey.api.CreateApiKeyCommand
import io.mailit.apikey.lang.plus
import io.mailit.apikey.spi.security.SecretHasher
import io.mailit.apikey.spi.security.SecureRandom
import java.time.Instant
import java.util.UUID

internal class ApiKeyFactory(
    private val secureRandom: SecureRandom,
    private val secretHasher: SecretHasher,
) {

    fun create(command: CreateApiKeyCommand): Pair<ApiKey, RawSecret> {
        val (secret, rawSecret) = generateSecret()

        val now = Instant.now()
        return ApiKey(
            id = generateId(),
            name = command.name,
            secret = secret,
            createdAt = now,
            expiresAt = now + command.expiration,
        ) to rawSecret
    }

    private fun generateId(): String {
        val uuid = UUID.randomUUID()

        return with(StringBuilder(22)) {
            appendWithAlphabeticalEncoding(uuid.mostSignificantBits)
            appendWithAlphabeticalEncoding(uuid.leastSignificantBits)
            toString()
        }
    }

    private tailrec fun StringBuilder.appendWithAlphabeticalEncoding(value: Long) {
        if (value != 0L) {
            val idx = value and (ALPHABET_SIZE - 1L)
            append(ALPHABET[idx.toInt()])
            appendWithAlphabeticalEncoding(value ushr ALPHABET_SIZE_BITS)
        }
    }

    private fun generateSecret(): Pair<ApiKeySecret, RawSecret> {
        val secret = secureRandom.generateInts(
            sequenceSize = SECRET_SIZE,
            upperBound = ALPHABET_SIZE,
        ).fold(StringBuilder(SECRET_SIZE)) { acc, idx -> acc.apply { append(ALPHABET[idx]) } }.toString()

        val hashedSecret = secretHasher.hash(secret)
        return ApiKeySecret(hashedSecret) to RawSecret(secret)
    }

    companion object {
        private const val ALPHABET_SIZE_BITS = 6
        private const val ALPHABET_SIZE = 1 shl ALPHABET_SIZE_BITS // 64
        private const val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-#"

        private const val SECRET_SIZE = 36

        init {
            assert(ALPHABET.length == ALPHABET_SIZE) { "ALPHABET doesn't match ALPHABET_SIZE" }
        }
    }
}
