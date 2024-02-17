package io.mailit.template.api

interface TemplateProcessor {

    /**
     * Merges an html message type template and [data]. Returns an html message as a string.
     */
    suspend fun process(mailTypeId: Long, templateEngine: TemplateEngine, data: Map<String, Any?>): Result<String>
}
