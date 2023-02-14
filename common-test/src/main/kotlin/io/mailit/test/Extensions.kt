package io.mailit.test

fun String.readResource(): String {
    return {}::class.java.classLoader.getResource(this)?.readText()
        ?: throw Exception("Resource: $this is not found")
}
