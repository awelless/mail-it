package io.mailit.template.core

import io.mailit.template.api.TemplateProcessor
import io.mailit.template.core.freemarker.FreemarkerTemplateProcessor
import io.mailit.template.core.none.NoneTemplateProcessor
import io.mailit.value.MailTypeId
import io.mailit.value.TemplateEngine
import io.mailit.value.TemplateEngine.FREEMARKER
import io.mailit.value.TemplateEngine.NONE

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
