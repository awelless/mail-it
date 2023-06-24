package io.mailit.core.service.admin.application

import io.mailit.core.admin.api.application.ApiKeyService
import io.mailit.core.admin.api.application.CreateApiKeyCommand
import io.mailit.core.exception.NotFoundException
import io.mailit.core.external.api.ApiKeyService as ExternalApiKeyService
import io.mailit.core.external.api.InvalidApiKeyException
import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.ApiKeyToken
import io.mailit.core.model.application.Application
import io.mailit.core.spi.application.ApiKeyRepository
import io.mailit.core.spi.application.ApplicationRepository
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import kotlin.time.toJavaDuration
import mu.KLogging

internal class ApiKeyServiceImpl(
    private val apiKeyGenerator: ApiKeyGenerator,
    private val apiKeyRepository: ApiKeyRepository,
    private val applicationRepository: ApplicationRepository,
    private val secretHasher: SecretHasher,
) : ApiKeyService, ExternalApiKeyService {

    override suspend fun generate(command: CreateApiKeyCommand): ApiKeyToken {
        val application = applicationRepository.findByIdOrThrow(command.applicationId)

        val id = apiKeyGenerator.generateId()
        val secret = apiKeyGenerator.generateSecret()

        val now = Instant.now()

        val apiKey = ApiKey(
            id = id,
            name = command.name,
            secret = secretHasher.hash(secret),
            application = application,
            createdAt = now,
            expiresAt = now.plus(command.expiration.toJavaDuration()),
        )

        apiKeyRepository.create(apiKey)

        logger.info { "ApiKey: $id, ${apiKey.name} for application: ${application.id} has been created" }

        return ApiKeyTokenCodec.encode(id, secret)
    }

    override suspend fun getAll(applicationId: Long) = apiKeyRepository.findAllByApplicationId(applicationId)

    override suspend fun delete(applicationId: Long, id: String) {
        val deleted = apiKeyRepository.delete(applicationId, id)

        if (!deleted) {
            throw NotFoundException("Api key with id: $id is not found")
        }

        logger.info { "ApiKey: $id fro application: $applicationId has been deleted" }
    }

    override suspend fun validate(token: ApiKeyToken): Application {
        val (id, secret) = ApiKeyTokenCodec.decode(token)

        val apiKey = apiKeyRepository.findById(id) ?: throw InvalidApiKeyException()

        if (apiKey.isExpired(Instant.now())) {
            logger.debug { "ApiKey: $id is expired" }
            throw InvalidApiKeyException()
        }

        if (!secretHasher.matches(secret, apiKey.secret)) {
            throw InvalidApiKeyException()
        }

        return apiKey.application
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
