package it.mail.domain.core.mailing.templates

import it.mail.domain.core.mailing.templates.freemarker.FreemarkerTemplateProcessor
import it.mail.domain.core.mailing.templates.none.NoneTemplateProcessor
import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.HtmlTemplateEngine.FREEMARKER
import it.mail.domain.model.HtmlTemplateEngine.NONE

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
