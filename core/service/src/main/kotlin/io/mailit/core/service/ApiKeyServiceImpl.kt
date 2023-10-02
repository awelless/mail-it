package io.mailit.core.service

import io.mailit.core.admin.api.ApiKeyService
import io.mailit.core.admin.api.CreateApiKeyCommand
import io.mailit.core.exception.NotFoundException
import io.mailit.core.exception.ValidationException
import io.mailit.core.external.api.ApiKeyService as ConnectorApiKeyService
import io.mailit.core.external.api.InvalidApiKeyException
import io.mailit.core.model.ApiKey
import io.mailit.core.model.ApiKeyToken
import io.mailit.core.spi.ApiKeyRepository
import io.mailit.core.spi.DuplicateUniqueKeyException
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import kotlin.time.toJavaDuration
import mu.KLogging

internal class ApiKeyServiceImpl(
    private val apiKeyGenerator: ApiKeyGenerator,
    private val apiKeyRepository: ApiKeyRepository,
    private val secretHasher: SecretHasher,
) : ApiKeyService, ConnectorApiKeyService {

    override suspend fun generate(command: CreateApiKeyCommand): ApiKeyToken {
        val id = apiKeyGenerator.generateId()
        val secret = apiKeyGenerator.generateSecret()

        val now = Instant.now()

        val apiKey = ApiKey(
            id = id,
            name = command.name,
            secret = secretHasher.hash(secret),
            createdAt = now,
            expiresAt = now.plus(command.expiration.toJavaDuration()),
        )

        try {
            apiKeyRepository.create(apiKey)
        } catch (e: DuplicateUniqueKeyException) {
            throw ValidationException("ApiKey name: ${command.name} is not unique", e)
        }

        logger.info { "ApiKey: $id, ${apiKey.name} has been created" }

        return ApiKeyTokenCodec.encode(id, secret)
    }

    override suspend fun getAll() = apiKeyRepository.findAll()

    override suspend fun delete(id: String) {
        val deleted = apiKeyRepository.delete(id)

        if (!deleted) {
            throw NotFoundException("Api key with id: $id is not found")
        }

        logger.info { "ApiKey: $id has been deleted" }
    }

    override suspend fun validate(token: ApiKeyToken): String {
        val (id, secret) = ApiKeyTokenCodec.decode(token)

        val apiKey = apiKeyRepository.findById(id) ?: throw InvalidApiKeyException()

        if (apiKey.isExpired(Instant.now())) {
            logger.debug { "ApiKey: $id is expired" }
            throw InvalidApiKeyException()
        }

        if (!secretHasher.matches(secret, apiKey.secret)) {
            throw InvalidApiKeyException()
        }

        return apiKey.name
    }

    companion object : KLogging()
}

internal class ApiKeyGenerator(
    private val random: SecureRandom,
) {

    fun generateId(): String {
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

    fun generateSecret() =
        random.ints(SECRET_SIZE.toLong(), 0, ALPHABET_SIZE).collect(
            { StringBuilder(SECRET_SIZE) },
            { builder, idx -> builder.append(ALPHABET[idx]) },
            StringBuilder::append,
        ).toString()

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

internal object ApiKeyTokenCodec {

    private val patternRegex = Regex("^mailit_(?<id>[a-zA-Z0-9-#]+)_(?<secret>[a-zA-Z0-9-#]+)$")

    fun encode(id: String, secret: String) = ApiKeyToken("mailit_${id}_$secret")

    fun decode(apiKeyToken: ApiKeyToken): Pair<String, String> {
        val match = patternRegex.matchEntire(apiKeyToken.value) ?: throw InvalidApiKeyException()
        val (id, secret) = match.destructured
        return id to secret
    }
}

internal interface SecretHasher {

    fun hash(raw: String): String

    fun matches(raw: String, hashed: String): Boolean
}
