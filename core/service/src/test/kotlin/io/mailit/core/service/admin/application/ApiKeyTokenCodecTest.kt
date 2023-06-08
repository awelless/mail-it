package io.mailit.core.service.admin.application

import io.mailit.core.external.api.InvalidApiKeyException
import io.mailit.core.model.application.ApiKeyToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ApiKeyTokenCodecTest {

    @Test
    fun `encode token`() {
        // given
        val id = "01234abcdeABCDE"
        val secret = "01232456789s3cr3t"

        // when
        val token = ApiKeyTokenCodec.encode(id, secret)

        // then
        assertEquals("mailit_${id}_$secret", token.value)
    }

    @Test
    fun `decode valid token`() {
        // given
        val id = "01234abcdeABCDE"
        val secret = "01232456789s3cr3t"

        // when
        val (actualId, actualSecret) = ApiKeyTokenCodec.decode(ApiKeyToken("mailit_${id}_$secret"))

        // then
        assertEquals(id, actualId)
        assertEquals(secret, actualSecret)
    }

    @ParameterizedTest
    @ValueSource(strings = ["mail_123_123", "mailit_123_", "mailit_123-123", "mailit#123_123"])
    fun `decode invalid token`(token: String) {
        assertThrows<InvalidApiKeyException> { ApiKeyTokenCodec.decode(ApiKeyToken(token)) }
    }
}
