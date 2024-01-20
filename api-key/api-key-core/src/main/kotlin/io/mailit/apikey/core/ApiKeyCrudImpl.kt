package io.mailit.apikey.core

import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.CreateApiKeyCommand
import io.mailit.apikey.lang.mapError
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.exception.NotFoundException
import io.mailit.core.exception.ValidationException
import mu.KLogging

internal class ApiKeyCrudImpl(
    private val apiKeyFactory: ApiKeyFactory,
    private val apiKeyRepository: ApiKeyRepository,
) : ApiKeyCrud {

    override suspend fun generate(command: CreateApiKeyCommand): Result<String> {
        val (apiKey, rawSecret) = apiKeyFactory.create(command)

        return apiKeyRepository.create(apiKey.toPersistenceModel())
            .onSuccess { logger.info { "ApiKey: $apiKey.id, ${apiKey.name} has been created" } }
            .mapError { e: DuplicateUniqueKeyException -> ValidationException("ApiKey name: ${command.name} is not unique", e) }
            .map { ApiKeyToken.encode(apiKey.id, rawSecret).value }
    }

    override suspend fun getAll() = apiKeyRepository.findAll().map { ApiKey.fromPersistenceModel(it).toApiModel() }

    override suspend fun delete(id: String) =
        if (apiKeyRepository.delete(id)) {
            logger.info { "ApiKey: $id has been deleted" }
            Result.success(Unit)
        } else {
            Result.failure(NotFoundException("Api key with id: $id is not found"))
        }

    companion object : KLogging()
}
