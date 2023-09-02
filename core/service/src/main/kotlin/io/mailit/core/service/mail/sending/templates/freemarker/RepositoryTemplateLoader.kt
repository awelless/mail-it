package io.mailit.core.service.mail.sending.templates.freemarker

import freemarker.cache.TemplateLoader
import io.mailit.core.exception.NotFoundException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.service.mail.sending.templates.InvalidTemplateEngineException
import io.mailit.core.spi.MailMessageTypeRepository
import java.io.StringReader
import kotlinx.coroutines.runBlocking

class RepositoryTemplateLoader(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) : TemplateLoader {

    override fun findTemplateSource(name: String): Any {
        val mailMessageType = runBlocking { mailMessageTypeRepository.findByName(name) }
            ?: throw NotFoundException("MailMessageType: $name is not found")

        val htmlMailMessageType = (mailMessageType as? HtmlMailMessageType)
            ?: throw InvalidTemplateEngineException("MailMessageType: $name is not for html messages")

        if (htmlMailMessageType.templateEngine != FREEMARKER) {
            throw InvalidTemplateEngineException("MailMessageType: $name doesn't use freemarker template")
        }

        return RepositoryTemplateSource(
            htmlMailMessageType.name,
            htmlMailMessageType.template,
            htmlMailMessageType.updatedAt.toEpochMilli(),
        )
    }

    override fun getLastModified(templateSource: Any) = (templateSource as RepositoryTemplateSource).updatedAt

    override fun getReader(templateSource: Any, encoding: String?) =
        (templateSource as? RepositoryTemplateSource)?.template?.value?.let { StringReader(it) }
            ?: throw IllegalArgumentException("templateSource of type ${templateSource::class.qualifiedName} is not supported")

    override fun closeTemplateSource(templateSource: Any?) {
        // no need to close RepositoryTemplateSource
    }

    override fun toString(): String {
        return "RepositoryTemplateLoader(repository=\"MailMessageTypeRepository\")"
    }
}

private data class RepositoryTemplateSource(
    val name: String,
    val template: MailMessageTemplate,
    val updatedAt: Long,
)
