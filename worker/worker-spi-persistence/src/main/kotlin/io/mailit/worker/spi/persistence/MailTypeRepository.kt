package io.mailit.worker.spi.persistence

import io.mailit.value.MailTypeId

interface MailTypeRepository {

    suspend fun findIdByName(name: String): MailTypeId?
}
