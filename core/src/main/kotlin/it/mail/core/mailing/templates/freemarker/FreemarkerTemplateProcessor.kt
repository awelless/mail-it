package it.mail.core.mailing.templates.freemarker

import freemarker.template.Configuration
import it.mail.core.mailing.templates.TemplateProcessor
import it.mail.core.model.HtmlMailMessageType
import java.io.StringWriter

class FreemarkerTemplateProcessor(
    private val configuration: Configuration,
) : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String {
        val template = configuration.getTemplate(mailMessageType.name)

        val writer = StringWriter()
        template.process(data, writer)

        return writer.toString()
    }
}
