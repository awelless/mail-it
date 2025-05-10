package io.mailit.template.core.none

import io.mailit.template.api.TemplateProcessor
import io.mailit.template.spi.persistence.TemplateRepository
import io.mailit.value.MailTypeId
import io.mailit.value.TemplateEngine
import io.mailit.value.exception.NotFoundException

/**
 * [TemplateProcessor] for [TemplateEngine.NONE].
 */
internal class NoneTemplateProcessor(
    private val templateRepository: TemplateRepository,
) {
    suspend fun process(mailTypeId: MailTypeId): Result<String> {
        val content = templateRepository.findByMailTypeId(mailTypeId)?.templateContent
        return content?.let { Result.success(it) } ?: Result.failure(NotFoundException("MailMessageType: $mailTypeId is not found"))
    }
}
