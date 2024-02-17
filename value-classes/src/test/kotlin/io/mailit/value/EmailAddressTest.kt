package io.mailit.value

import io.mailit.value.EmailAddress.Companion.toEmailAddress
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmailAddressTest {

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
    fun `toEmail - when valid emails are passed - creates`(string: String) {
        val email = string.toEmailAddress()

        assertEquals(string, email.email)
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
    fun `toEmail - when invalid emails are passed - throwsException`(string: String) {
        val ex = assertThrows<IllegalArgumentException> { string.toEmailAddress() }

        assertEquals("Invalid email format", ex.message)
    }
}
