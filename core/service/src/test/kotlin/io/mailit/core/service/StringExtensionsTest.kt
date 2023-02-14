package io.mailit.core.service

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class StringExtensionsTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "email123@example.com",
            "firstname.lastname@example.com",
            "firstname+lastname@example.com",
            "email@example-one.com",
            "email@123.123.123.123",
        ],
    )
    fun `isEmail - when valid emails are passed - return true`(string: String) {
        assertTrue(string.isEmail())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "plainaddress",
            "@example.com",
            "email.example.com",
            "email@example@example.com",
            "email@example..com",
            "Joe Smith <email@example.com>",
        ],
    )
    fun `isEmail - when invalid emails are passed - return false`(string: String) {
        assertFalse(string.isEmail())
    }
}
