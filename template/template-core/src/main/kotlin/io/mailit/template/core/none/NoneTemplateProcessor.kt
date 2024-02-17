package io.mailit.template.core.none

import io.mailit.core.exception.NotFoundException
import io.mailit.template.api.TemplateEngine
import io.mailit.template.api.TemplateProcessor
import io.mailit.template.spi.persistence.TemplateRepository

/**
 * [TemplateProcessor] for [TemplateEngine.NONE].
 */
internal class NoneTemplateProcessor(
    private val templateRepository: TemplateRepository,
) {
    suspend fun process(mailTypeId: Long): Result<String> {
        val content = templateRepository.findByMailTypeId(mailTypeId)?.templateContent
        return content?.let { Result.success(it) } ?: Result.failure(NotFoundException("MailMessageType: $mailTypeId is not found"))
    }
}
