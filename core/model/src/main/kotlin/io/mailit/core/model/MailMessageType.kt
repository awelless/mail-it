package io.mailit.core.model

import io.mailit.value.MailTypeId
import io.mailit.value.MailTypeState
import io.mailit.value.TemplateEngine
import java.time.Instant

/**
 * Type of [MailMessage]. Contains general info for all mails of one type
 */
sealed class MailMessageType(

    open val id: MailTypeId,

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

    open var state: MailTypeState,

    open val createdAt: Instant,

    open var updatedAt: Instant,
)

/**
 * [MailMessageType] for plain "text" messages
 */
data class PlainTextMailMessageType(
    override val id: MailTypeId,
    override val name: String,
    override var description: String? = null,
    override var maxRetriesCount: Int? = null,
    override var state: MailTypeState = MailTypeState.ACTIVE,
    override val createdAt: Instant,
    override var updatedAt: Instant,
) : MailMessageType(id, name, description, maxRetriesCount, state, createdAt, updatedAt)

/**
 * [MailMessageType] for "html" messages
 */
data class HtmlMailMessageType(
    override val id: MailTypeId,
    override val name: String,
    override var description: String? = null,
    override var maxRetriesCount: Int? = null,
    override var state: MailTypeState = MailTypeState.ACTIVE,
    override val createdAt: Instant,
    override var updatedAt: Instant,

    var templateEngine: TemplateEngine,

    /**
     * Template that will be used by [templateEngine]
     */
    var template: MailMessageTemplate,
) : MailMessageType(id, name, description, maxRetriesCount, state, createdAt, updatedAt)
