package it.mail.domain.core.mailing.templates.none

import it.mail.domain.core.mailing.templates.TemplateProcessor
import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.HtmlTemplateEngine

/**
 * [TemplateProcessor] for [HtmlMailMessageType] with [HtmlTemplateEngine.NONE]
 */
class NoneTemplateProcessor : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String = mailMessageType.template
}
