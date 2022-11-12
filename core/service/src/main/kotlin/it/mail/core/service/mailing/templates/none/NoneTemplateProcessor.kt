package it.mail.core.service.mailing.templates.none

import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine
import it.mail.core.service.mailing.templates.TemplateProcessor

/**
 * [TemplateProcessor] for [HtmlMailMessageType] with [HtmlTemplateEngine.NONE]
 */
class NoneTemplateProcessor : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String = mailMessageType.template
}
