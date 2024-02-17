package io.mailit.template.test

import io.mailit.template.api.TemplateProcessor
import io.mailit.value.MailTypeId
import io.mailit.value.TemplateEngine

class StubTemplateProcessor(val html: String) : TemplateProcessor {
    override suspend fun process(mailTypeId: MailTypeId, templateEngine: TemplateEngine, data: Map<String, Any?>) = Result.success(html)
}
