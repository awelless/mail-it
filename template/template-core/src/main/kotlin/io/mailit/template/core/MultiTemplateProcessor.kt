package io.mailit.template.core

import io.mailit.template.api.TemplateEngine
import io.mailit.template.api.TemplateEngine.FREEMARKER
import io.mailit.template.api.TemplateEngine.NONE
import io.mailit.template.api.TemplateProcessor
import io.mailit.template.core.freemarker.FreemarkerTemplateProcessor
import io.mailit.template.core.none.NoneTemplateProcessor
import io.mailit.value.MailTypeId

internal class MultiTemplateProcessor(
    private val noneTemplateProcessor: NoneTemplateProcessor,
    private val freemarkerTemplateProcessor: FreemarkerTemplateProcessor,
) : TemplateProcessor {

    override suspend fun process(mailTypeId: MailTypeId, templateEngine: TemplateEngine, data: Map<String, Any?>): Result<String> =
        when (templateEngine) {
            NONE -> noneTemplateProcessor.process(mailTypeId)
            FREEMARKER -> freemarkerTemplateProcessor.process(mailTypeId, data)
        }
}
