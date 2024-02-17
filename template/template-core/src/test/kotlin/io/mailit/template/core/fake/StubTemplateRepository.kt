package io.mailit.template.core.fake

import io.mailit.template.spi.persistence.PersistenceTemplate
import io.mailit.template.spi.persistence.TemplateRepository

class StubTemplateRepository(
    private val templates: Map<Long, PersistenceTemplate>,
) : TemplateRepository {

    constructor(vararg templates: Pair<Long, PersistenceTemplate>) : this(templates.toMap())

    override suspend fun findByMailTypeId(mailTypeId: Long) = templates[mailTypeId]
}
