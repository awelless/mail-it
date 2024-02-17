package io.mailit.template.core.freemarker

import freemarker.core.InvalidReferenceException
import freemarker.core.ParseException
import io.mailit.core.exception.NotFoundException
import io.mailit.template.core.fake.StubTemplateRepository
import io.mailit.template.spi.persistence.PersistenceTemplate
import io.mailit.test.assertHtmlEquals
import io.mailit.test.readResource
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val FREEMARKER_TEST_TEMPLATE_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/template.ftlh"
private const val FREEMARKER_INVALID_TEMPLATE_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/invalid_template.ftlh"
private const val EXPECTED_HTML_PATH = "templates/freemarker/FreemarkerTemplateProcessorTest/expected.html"

class FreemarkerTemplateProcessorTest {

    private val mailTypeId = 1L
    private val template = PersistenceTemplate(mailTypeId, FREEMARKER_TEST_TEMPLATE_PATH.readResource(), Instant.now())

    private val invalidMailTypeId = 2L
    private val invalidTemplate = PersistenceTemplate(invalidMailTypeId, FREEMARKER_INVALID_TEMPLATE_PATH.readResource(), Instant.now())

    private val templateRepository = StubTemplateRepository(mailTypeId to template, invalidMailTypeId to invalidTemplate)
    private val freemarker = createFreemarkerTemplateProcessor(templateRepository)

    @Test
    fun `process valid template - creates html`() = runTest {
        // given
        // Data is in a form of maps and lists only.
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
        val actual = freemarker.process(mailTypeId, data).getOrThrow()

        // then
        assertHtmlEquals(EXPECTED_HTML_PATH.readResource(), actual)
    }

    @Test
    fun `process valid template with excess data - creates html`() = runTest {
        // given
        // Data is in a form of maps and lists only.
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
        val actual = freemarker.process(mailTypeId, data).getOrThrow()

        // then
        assertHtmlEquals(EXPECTED_HTML_PATH.readResource(), actual)
    }

    @Test
    fun `process valid template when some data is not present - fails`() = runTest {
        // given
        val data = mapOf(
            "title" to "Created html",
        )

        // when
        val error = freemarker.process(mailTypeId, data).exceptionOrNull()

        // then
        assertTrue(error is InvalidReferenceException)
    }

    @Test
    fun `template doesn't exist - fails`() = runTest {
        // when
        val error = freemarker.process(mailTypeId = 999L, emptyMap()).exceptionOrNull()

        // then
        assertTrue(error is NotFoundException)
    }

    @Test
    fun `template is invalid - fails`() = runTest {
        // when
        val error = freemarker.process(invalidMailTypeId, emptyMap()).exceptionOrNull()

        // then
        assertTrue(error is ParseException)
    }
}
