package io.mailit.test

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertTrue

fun assertHtmlEquals(expected: String, actual: String) {
    val expectedDocument = Jsoup.parse(expected)
    val actualDocument = Jsoup.parse(actual)

    assertTrue(expectedDocument.hasSameValue(actualDocument), "Html contents are different")
}
