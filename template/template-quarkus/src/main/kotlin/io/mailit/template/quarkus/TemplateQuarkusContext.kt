package io.mailit.template.quarkus

import io.mailit.template.context.TemplateContext
import io.mailit.template.spi.persistence.TemplateRepository
import jakarta.inject.Singleton

class TemplateQuarkusContext(
    templateRepository: TemplateRepository,
) {
    private val templateContext = TemplateContext.create(templateRepository)

    @Singleton
    fun templateProcess() = templateContext.templateProcessor
}
