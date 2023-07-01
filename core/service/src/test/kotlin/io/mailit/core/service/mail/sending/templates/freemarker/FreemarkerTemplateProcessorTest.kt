package io.mailit.core.service.mail.sending.templates.freemarker

import freemarker.cache.StringTemplateLoader
import freemarker.core.InvalidReferenceException
import freemarker.core.ParseException
import freemarker.template.TemplateNotFoundException
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.FREEMARKER
import io.mailit.test.assertHtmlEquals
import io.mailit.test.createHtmlMailMessageType
import io.mailit.test.readResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

const val FREEMARKER_TEST_TEMPLATE_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/template.ftlh"
const val FREEMARKER_INVALID_TEMPLATE_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/invalid_template.ftlh"
const val EXPECTED_HTML_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/expected.html"

open class FreemarkerTemplateProcessorTest {

    val templateLoader = StringTemplateLoader()
    val configuration = Configuration(templateLoader)
    val templateProcessor = FreemarkerTemplateProcessor(configuration)

    lateinit var mailMessageType: HtmlMailMessageType

    @BeforeEach
    fun setUp() {
        mailMessageType = createHtmlMailMessageType()
        mailMessageType.templateEngine = FREEMARKER
    }

    @Test
    fun `process valid template - creates html`() {
        // given
        templateLoader.putTemplate(mailMessageType.name, FREEMARKER_TEST_TEMPLATE_PATH.readResource())

        // data in forms of maps and lists only
        val data = mapOf(
            "title" to "Created html",
            "file" to mapOf(
                "name" to "Sample",
                "developer" to "me",
            ),
            "systems" to listOf(
                mapOf(
                    "name" to "Android",
                    "developer" to "Google",
                ),
                mapOf(
                    "name" to "iOS",
                    "developer" to "Apple",
                ),
                mapOf(
                    "name" to "Ubuntu",
                    "developer" to "Canonical",
                ),
                mapOf(
                    "name" to "Windows7",
                    "developer" to "Microsoft",
                ),
            ),
        )

        // when
        val actual = templateProcessor.process(mailMessageType, data)

        // then
        assertHtmlEquals(EXPECTED_HTML_PATH.readResource(), actual)
    }

    @Test
    fun `process valid template with excess data - creates html`() {
        // given
        templateLoader.putTemplate(mailMessageType.name, FREEMARKER_TEST_TEMPLATE_PATH.readResource())

        // data in forms of maps and lists only
        val data = mapOf(
            "title" to "Created html",
            "file" to mapOf(
                "name" to "Sample",
                "developer" to "me",
            ),
            "systems" to listOf(
                mapOf(
                    "name" to "Android",
                    "developer" to "Google",
                ),
                mapOf(
                    "name" to "iOS",
                    "developer" to "Apple",
                ),
                mapOf(
                    "name" to "Ubuntu",
                    "developer" to "Canonical",
                ),
                mapOf(
                    "name" to "Windows7",
                    "developer" to "Microsoft",
                ),
            ),
            "notUsedData" to "123123",
            "anotherNotUsedData" to 100,
        )

        // when
        val actual = templateProcessor.process(mailMessageType, data)

        // then
        assertHtmlEquals(EXPECTED_HTML_PATH.readResource(), actual)
    }

    @Test
    fun `process valid template when some data is not present - throws exception`() {
        // given
        templateLoader.putTemplate(mailMessageType.name, FREEMARKER_TEST_TEMPLATE_PATH.readResource())

        val data = mapOf(
            "title" to "Created html",
        )

        // when
        assertThrows<InvalidReferenceException> { templateProcessor.process(mailMessageType, data) }
    }

    @Test
    fun `template doesn't exist - throws exception`() {
        assertThrows<TemplateNotFoundException> { templateProcessor.process(mailMessageType, emptyMap()) }
    }

    @Test
    fun `template is invalid - throws exception`() {
        // given
        templateLoader.putTemplate(mailMessageType.name, FREEMARKER_INVALID_TEMPLATE_PATH.readResource())

        // when
        assertThrows<ParseException> { templateProcessor.process(mailMessageType, emptyMap()) }
    }
}
