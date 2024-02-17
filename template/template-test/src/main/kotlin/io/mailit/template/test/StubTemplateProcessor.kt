package io.mailit.template.test

import io.mailit.template.api.TemplateEngine
import io.mailit.template.api.TemplateProcessor

class StubTemplateProcessor(val html: String) : TemplateProcessor {
    override suspend fun process(mailTypeId: Long, templateEngine: TemplateEngine, data: Map<String, Any?>) = Result.success(html)
}
