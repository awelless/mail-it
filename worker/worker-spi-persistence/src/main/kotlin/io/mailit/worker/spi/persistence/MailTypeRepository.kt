package io.mailit.worker.spi.persistence

import io.mailit.value.MailTypeId

interface MailTypeRepository {

    suspend fun findActiveIdByName(name: String): MailTypeId?
}
