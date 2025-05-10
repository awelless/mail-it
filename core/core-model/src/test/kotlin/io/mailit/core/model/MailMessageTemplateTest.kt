package io.mailit.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MailMessageTemplateTest {

    @Test
    fun `compress template`() {
        val origin = MailMessageTemplate("<html></html>")
        val compressed = origin.compressedValue
        val uncompressed = MailMessageTemplate.fromCompressedValue(compressed)

        assertEquals(origin, uncompressed)
    }
}
