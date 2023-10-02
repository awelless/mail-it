package io.mailit.core.service.mail.sending.templates.freemarker

import freemarker.template.Configuration
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.service.mail.sending.templates.TemplateProcessor
import java.io.StringWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FreemarkerTemplateProcessor(
    private val configuration: Configuration,
) : TemplateProcessor {

    override suspend fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String {
        val template = withContext(Dispatchers.IO) { configuration.getTemplate(mailMessageType.name) }

        val writer = StringWriter()
        template.process(data, writer)

        return writer.toString()
    }
}
