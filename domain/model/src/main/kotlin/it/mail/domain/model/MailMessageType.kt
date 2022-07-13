package it.mail.domain.model

import java.time.Instant

/**
 * Type of [MailMessage]. Contains general info for all mails of one type
 */
sealed class MailMessageType(

    open var id: Long,

    /**
     * Name of actual mail type. Used in external API
     * to determine a type of mail that is sent.
     * Unique and non-updatable
     */
    open val name: String,

    open var description: String?,

    /**
     * Number of max mail send retry attempts.
     * If null, then infinite retries are possible
     */
    open var maxRetriesCount: Int?,

    open var state: MailMessageTypeState,

    open val createdAt: Instant,

    open var updatedAt: Instant,
)

enum class MailMessageTypeState {

    ENABLED,
    DELETED,
    FORCE_DELETED,
}

/**
 * [MailMessageType] for plain "text" messages
 */
data class PlainTextMailMessageType(

    override var id: Long = 0,
    override val name: String,
    override var description: String? = null,
    override var maxRetriesCount: Int? = null,
    override var state: MailMessageTypeState = MailMessageTypeState.ENABLED,
    override val createdAt: Instant,
    override var updatedAt: Instant,

) : MailMessageType(id, name, description, maxRetriesCount, state, createdAt, updatedAt)

/**
 * [MailMessageType] for "html" messages
 */
data class HtmlMailMessageType(

    override var id: Long = 0,
    override val name: String,
    override var description: String? = null,
    override var maxRetriesCount: Int? = null,
    override var state: MailMessageTypeState = MailMessageTypeState.ENABLED,
    override val createdAt: Instant,
    override var updatedAt: Instant,

    var templateEngine: HtmlTemplateEngine,

    /**
     * Template that will be used by [templateEngine]
     */
    var template: String,

) : MailMessageType(id, name, description, maxRetriesCount, state, createdAt, updatedAt)

enum class HtmlTemplateEngine {

    /**
     * Plain html, template is sent as it is, without any transformations
     */
    NONE,

    /**
     * [Freemarker docs](https://freemarker.apache.org/)
     */
    FREEMARKER,
}
