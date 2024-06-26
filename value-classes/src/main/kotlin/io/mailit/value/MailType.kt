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

enum class MailTypeState {
    /**
     * Normal mail type state. Mails are sent normally.
     */
    ACTIVE,

    /**
     * Mail type has been deleted. New mails are not accepted. Already created mails will still be sent.
     */
    DELETED,

    /**
     * Mail type has been deleted. New mails are not accepted. Already created mails will be [MailState.CANCELED].
     */
    FORCE_DELETED,
}
