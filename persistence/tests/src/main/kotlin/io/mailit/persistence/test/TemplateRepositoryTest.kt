package io.mailit.persistence.test

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.template.spi.persistence.TemplateRepository
import io.mailit.test.createHtmlMailMessageType
import io.mailit.value.MailTypeId
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class TemplateRepositoryTest {

    @Inject
    lateinit var templateRepository: TemplateRepository

    @Inject
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    lateinit var mailMessageType: HtmlMailMessageType

    @BeforeEach
    fun setUp() {
        runBlocking {
            mailMessageType = createHtmlMailMessageType()
            mailMessageTypeRepository.create(mailMessageType)
        }
    }

    @Test
    fun `findByMailId - when exists`() = runTest {
        // when
        val template = templateRepository.findByMailTypeId(mailMessageType.id)

        // then
        assertEquals(mailMessageType.id, template?.mailTypeId)
        assertEquals(mailMessageType.template.value, template?.templateContent)
    }

    @Test
    fun `findByMailId - when doesn't exist`() = runTest {
        // when
        val template = templateRepository.findByMailTypeId(MailTypeId(0))

        // then
        assertNull(template)
    }
}
