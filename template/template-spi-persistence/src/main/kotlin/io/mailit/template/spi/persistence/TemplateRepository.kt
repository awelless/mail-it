package io.mailit.template.spi.persistence

import io.mailit.value.MailTypeId
import java.time.Instant

interface TemplateRepository {

    suspend fun findByMailTypeId(mailTypeId: MailTypeId): PersistenceTemplate?
}

data class PersistenceTemplate(
    val mailTypeId: MailTypeId,
    val templateContent: String,
    val updatedAt: Instant,
)
