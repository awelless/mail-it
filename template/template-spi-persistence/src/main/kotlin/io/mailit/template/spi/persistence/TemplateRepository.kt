package io.mailit.template.spi.persistence

import java.time.Instant

interface TemplateRepository {

    suspend fun findByMailTypeId(mailTypeId: Long): PersistenceTemplate?
}

data class PersistenceTemplate(
    val mailTypeId: Long,
    val templateContent: String,
    val updatedAt: Instant,
)
