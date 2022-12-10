package it.mail.core.service.mailing.templates.freemarker

import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine.FREEMARKER
import it.mail.core.model.HtmlTemplateEngine.NONE
import it.mail.core.service.mailing.templates.InvalidTemplateEngineException
import it.mail.core.spi.MailMessageTypeRepository
import it.mail.test.createHtmlMailMessageType
import it.mail.test.createPlainMailMessageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RepositoryTemplateLoaderTest {

    @MockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var repositoryTemplateLoader: RepositoryTemplateLoader

    lateinit var mailMessageType: HtmlMailMessageType

    @BeforeEach
    fun setUp() {
        mailMessageType = createHtmlMailMessageType()
    }

    @Test
    fun findTemplateSource_freemarkerHtmlType() {
        // given
        mailMessageType.templateEngine = FREEMARKER
        coEvery { mailMessageTypeRepository.findByName(mailMessageType.name) }.returns(mailMessageType)

        // when
        val source = repositoryTemplateLoader.findTemplateSource(mailMessageType.name)
        val lastModified = repositoryTemplateLoader.getLastModified(source)
        val template = repositoryTemplateLoader.getReader(source, null).readText()

        // then
        assertEquals(mailMessageType.updatedAt.toEpochMilli(), lastModified)
        assertEquals(mailMessageType.template, template)
    }

    @Test
    fun findTemplateSource_notFreemarkerTemplateEngine_throwsException() {
        // given
        mailMessageType.templateEngine = NONE
        coEvery { mailMessageTypeRepository.findByName(mailMessageType.name) }.returns(mailMessageType)

        // when
        val exception = assertThrows<InvalidTemplateEngineException> { repositoryTemplateLoader.findTemplateSource(mailMessageType.name) }

        // then
        assertEquals("MailMessageType: ${mailMessageType.name} doesn't use freemarker template", exception.message)
    }

    @Test
    fun findTemplateSource_plainMailMessageType_throwsException() {
        // given
        val plainMailMessageType = createPlainMailMessageType()
        coEvery { mailMessageTypeRepository.findByName(mailMessageType.name) }.returns(plainMailMessageType)

        // when
        val exception = assertThrows<InvalidTemplateEngineException> { repositoryTemplateLoader.findTemplateSource(mailMessageType.name) }

        // then
        assertEquals("MailMessageType: ${mailMessageType.name} is not for html messages", exception.message)
    }
}
