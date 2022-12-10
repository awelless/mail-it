package it.mail.persistence.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SliceCreatorTest {

    @Test
    fun createSlice_whenNotTheLast() {
        // given
        val page = 1
        val size = 10
        val content = (0..size).toList()

        // when
        val actual = createSlice(content, page, size)

        // then
        assertEquals(content.take(content.size - 1), actual.content)
        assertEquals(size, actual.size)
        assertEquals(page, actual.page)
        assertFalse(actual.last)
    }

    @Test
    fun createSlice_whenTheLast() {
        // given
        val page = 1
        val size = 10
        val content = listOf(1, 2, 3)

        // when
        val actual = createSlice(content, page, size)

        // then
        assertEquals(content, actual.content)
        assertEquals(size, actual.size)
        assertEquals(page, actual.page)
        assertTrue(actual.last)
    }
}
