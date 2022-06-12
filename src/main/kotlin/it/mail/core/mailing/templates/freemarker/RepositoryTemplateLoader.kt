package it.mail.core.mailing.templates.freemarker

import freemarker.cache.TemplateLoader
import it.mail.core.NotFoundException
import it.mail.core.mailing.templates.InvalidTemplateEngineException
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine.FREEMARKER
import it.mail.persistence.api.MailMessageTypeRepository
import kotlinx.coroutines.runBlocking
import java.io.StringReader

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

    override fun getReader(templateSource: Any, encoding: String?) = StringReader((templateSource as RepositoryTemplateSource).template)

    override fun closeTemplateSource(templateSource: Any?) {
        // no need to close RepositoryTemplateSource
    }

    override fun toString(): String {
        return "RepositoryTemplateLoader(repository=\"MailMessageTypeRepository\")"
    }
}

private data class RepositoryTemplateSource(
    val name: String,
    val template: String,
    val updatedAt: Long,
)
