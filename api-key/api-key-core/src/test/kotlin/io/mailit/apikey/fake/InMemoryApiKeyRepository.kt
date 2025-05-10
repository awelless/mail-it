package io.mailit.apikey.fake

import io.mailit.apikey.spi.persistence.ApiKey
import io.mailit.apikey.spi.persistence.ApiKeyRepository
import io.mailit.value.exception.DuplicateUniqueKeyException
import java.util.concurrent.ConcurrentHashMap

class InMemoryApiKeyRepository : ApiKeyRepository {

    private val id2ApiKey = ConcurrentHashMap<String, ApiKey>()

    override suspend fun findById(id: String) = id2ApiKey[id]

    override suspend fun findAll() = id2ApiKey.values.toList()

    override suspend fun create(apiKey: ApiKey) = if (id2ApiKey.putIfAbsent(apiKey.id, apiKey) == null) {
        Result.success(Unit)
    } else {
        Result.failure(DuplicateUniqueKeyException("duplicate apikey id", null))
    }

    override suspend fun delete(id: String) = id2ApiKey.remove(id) != null
}
