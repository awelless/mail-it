package io.mailit.template.context

import io.mailit.template.api.TemplateProcessor
import io.mailit.template.core.MultiTemplateProcessor
import io.mailit.template.core.freemarker.createFreemarkerTemplateProcessor
import io.mailit.template.core.none.NoneTemplateProcessor
import io.mailit.template.spi.persistence.TemplateRepository

class TemplateContext private constructor(
    val templateProcessor: TemplateProcessor,
) {
    companion object {
        fun create(templateRepository: TemplateRepository): TemplateContext {
            val multiTemplateProcessor = MultiTemplateProcessor(
                noneTemplateProcessor = NoneTemplateProcessor(templateRepository),
                freemarkerTemplateProcessor = createFreemarkerTemplateProcessor(templateRepository),
            )

            return TemplateContext(multiTemplateProcessor)
        }
    }
}
