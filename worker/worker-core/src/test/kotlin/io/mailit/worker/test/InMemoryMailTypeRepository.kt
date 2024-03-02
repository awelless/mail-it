package io.mailit.worker.test

import io.mailit.value.MailTypeId
import io.mailit.worker.spi.persistence.MailTypeRepository

class InMemoryMailTypeRepository : MailTypeRepository {

    private val types = mutableMapOf<MailTypeId, String>()

    fun addType(id: MailTypeId, name: String) {
        types[id] = name
    }

    override suspend fun findIdByName(name: String) = types.filterValues { it == name }.entries.firstOrNull()?.key
}
