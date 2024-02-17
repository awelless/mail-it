package io.mailit.template.api

import io.mailit.value.MailTypeId

interface TemplateProcessor {

    /**
     * Merges an html message type template and [data]. Returns an html message as a string.
     */
    suspend fun process(mailTypeId: MailTypeId, templateEngine: TemplateEngine, data: Map<String, Any?>): Result<String>
}
