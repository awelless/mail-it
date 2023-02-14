package io.mailit.core.service.mailing.templates

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.core.model.HtmlTemplateEngine.NONE
import io.mailit.core.service.mailing.templates.freemarker.FreemarkerTemplateProcessor
import io.mailit.core.service.mailing.templates.none.NoneTemplateProcessor

interface TemplateProcessor {

    /**
     * Merges [HtmlMailMessageType.template] and [data]. Returns html message
     */
    fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String
}

class TemplateProcessorManager(
    private val noneTemplateProcessor: NoneTemplateProcessor,
    private val freemarkerTemplateProcessor: FreemarkerTemplateProcessor,
) : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String =
        when (mailMessageType.templateEngine) {
            NONE -> noneTemplateProcessor.process(mailMessageType, data)
            FREEMARKER -> freemarkerTemplateProcessor.process(mailMessageType, data)
        }
}
