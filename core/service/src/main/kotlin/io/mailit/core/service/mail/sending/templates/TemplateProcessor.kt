package io.mailit.core.service.mail.sending.templates

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.core.model.HtmlTemplateEngine.NONE
import io.mailit.core.service.mail.sending.templates.freemarker.FreemarkerTemplateProcessor
import io.mailit.core.service.mail.sending.templates.none.NoneTemplateProcessor

interface TemplateProcessor {

    /**
     * Merges [HtmlMailMessageType.template] and [data]. Returns html message
     */
    suspend fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String
}

class TemplateProcessorManager(
    private val noneTemplateProcessor: NoneTemplateProcessor,
    private val freemarkerTemplateProcessor: FreemarkerTemplateProcessor,
) : TemplateProcessor {

    override suspend fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String =
        when (mailMessageType.templateEngine) {
            NONE -> noneTemplateProcessor.process(mailMessageType, data)
            FREEMARKER -> freemarkerTemplateProcessor.process(mailMessageType, data)
        }
}
