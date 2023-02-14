package io.mailit.core.service.mailing.templates.none

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine
import io.mailit.core.service.mailing.templates.TemplateProcessor

/**
 * [TemplateProcessor] for [HtmlMailMessageType] with [HtmlTemplateEngine.NONE]
 */
class NoneTemplateProcessor : TemplateProcessor {

    override fun process(mailMessageType: HtmlMailMessageType, data: Map<String, Any?>): String = mailMessageType.template
}
