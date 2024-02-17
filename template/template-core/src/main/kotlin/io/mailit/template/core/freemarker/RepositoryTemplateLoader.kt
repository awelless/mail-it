package io.mailit.template.core.freemarker

import freemarker.cache.TemplateLoader
import io.mailit.core.exception.NotFoundException
import io.mailit.template.spi.persistence.TemplateRepository
import io.mailit.value.MailTypeId
import java.io.StringReader
import kotlinx.coroutines.runBlocking

internal class RepositoryTemplateLoader(
    private val templateRepository: TemplateRepository,
) : TemplateLoader {

    private val nameRegex = Regex("^(\\d+).*$")

    override fun findTemplateSource(name: String): Any {
        val mailTypeId = name.toMailTypeId()

        val template = runBlocking { templateRepository.findByMailTypeId(mailTypeId) }
            ?: throw NotFoundException("MailMessageType: $mailTypeId is not found")

        return RepositoryTemplateSource(
            mailTypeId,
            template.templateContent,
            template.updatedAt.toEpochMilli(),
        )
    }

    private fun String.toMailTypeId(): MailTypeId {
        val match = nameRegex.matchEntire(this) ?: throw IllegalArgumentException("Invalid template name: $this")
        val longValue = match.groupValues[1].toLong()
        return MailTypeId(longValue)
    }

    override fun getLastModified(templateSource: Any) = templateSource.asTemplateSource().updatedAtMillis

    override fun getReader(templateSource: Any, encoding: String?) =
        StringReader(templateSource.asTemplateSource().templateContent)

    private fun Any.asTemplateSource() =
        (this as? RepositoryTemplateSource) ?: throw IllegalArgumentException("Template source is of invalid type: ${this::class.qualifiedName}")

    override fun closeTemplateSource(templateSource: Any?) {
        // no need to close RepositoryTemplateSource
    }

    override fun toString(): String {
        return "RepositoryTemplateLoader"
    }
}

private data class RepositoryTemplateSource(
    val mailTypeId: MailTypeId,
    val templateContent: String,
    val updatedAtMillis: Long,
)
