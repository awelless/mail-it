package it.mail.core.mailing.templates.none

import it.mail.core.mailing.templates.TemplateProcessor
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine

/**
 * [TemplateProcessor] for [HtmlMailMessageType] with [HtmlTemplateEngine.NONE]
 */
class NoneTemplateProcessor : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String = mailMessageType.template
}
