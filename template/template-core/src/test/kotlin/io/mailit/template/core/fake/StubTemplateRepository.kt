package io.mailit.template.core.fake

import io.mailit.template.spi.persistence.PersistenceTemplate
import io.mailit.template.spi.persistence.TemplateRepository
import io.mailit.value.MailTypeId

class StubTemplateRepository(
    private val templates: Map<MailTypeId, PersistenceTemplate>,
) : TemplateRepository {

    constructor(vararg templates: Pair<MailTypeId, PersistenceTemplate>) : this(templates.toMap())

    override suspend fun findByMailTypeId(mailTypeId: MailTypeId) = templates[mailTypeId]
}
