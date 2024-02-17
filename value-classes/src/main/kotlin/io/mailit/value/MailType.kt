package io.mailit.value

@JvmInline
value class MailTypeId(val value: Long)

enum class TemplateEngine {
    /**
     * Plain html, template is sent as it is, without any transformations.
     */
    NONE,

    /**
     * [Freemarker docs](https://freemarker.apache.org/).
     */
    FREEMARKER,
}
