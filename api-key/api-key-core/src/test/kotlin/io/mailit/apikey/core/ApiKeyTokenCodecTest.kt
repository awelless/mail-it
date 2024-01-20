package io.mailit.apikey.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ApiKeyTokenCodecTest {

    @Test
    fun `encode token`() {
        // given
        val id = "01234abcdeABCDE"
        val secret = RawSecret("01232456789s3cr3t")

        // when
        val token = ApiKeyToken.encode(id, secret)

        // then
        assertEquals("mailit_${id}_${secret.value}", token.value)
    }

    @Test
    fun `decode valid token`() {
        // given
        val id = "01234abcdeABCDE"
        val secret = RawSecret("01232456789s3cr3t")

        // when
        val (actualId, actualSecret) = ApiKeyToken("mailit_${id}_${secret.value}").decode().getOrThrow()

        // then
        assertEquals(id, actualId)
        assertEquals(secret, actualSecret)
    }

    @ParameterizedTest
    @ValueSource(strings = ["mail_123_123", "mailit_123_", "mailit_123-123", "mailit#123_123"])
    fun `decode invalid token`(token: String) {
        assertTrue(ApiKeyToken(token).decode().isFailure)
    }
}
