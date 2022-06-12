package it.mail.core.mailing.templates

import it.mail.core.mailing.templates.none.NoneTemplateProcessor
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine.FREEMARKER
import it.mail.core.model.HtmlTemplateEngine.NONE

interface TemplateProcessor {

    /**
     * Merges [HtmlMailMessageType.template] and [data]. Returns html message
     */
    fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String
}

class TemplateProcessorManager(
    private val noneTemplateProcessor: NoneTemplateProcessor,
) : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String =
        when (mailMessageType.templateEngine) {
            NONE -> noneTemplateProcessor.process(mailMessageType, data)
            FREEMARKER -> throw Exception("Freemarker template engine is not supported yet")
        }
}
