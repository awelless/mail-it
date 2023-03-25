package io.mailit.test.restassured

import io.restassured.response.ValidatableResponse
import org.hamcrest.Matcher
import org.hamcrest.Matchers

infix fun String.equalTo(value: Any?) = Pair(this, Matchers.equalTo(value))

fun ValidatableResponse.body(matcherPair: Pair<String, Matcher<*>>, vararg additionalPairs: Pair<String, Matcher<*>>): ValidatableResponse {
    val (path, matcher) = matcherPair
    val additionalKeyMatcherPairs = additionalPairs.flatMap { (path, matcher) -> listOf(path, matcher) }.toTypedArray()
    return body(path, matcher, *additionalKeyMatcherPairs)
}
