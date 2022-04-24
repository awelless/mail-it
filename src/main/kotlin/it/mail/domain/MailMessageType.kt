package it.mail.domain

/**
 * Type of [MailMessage]. Contains general info for all mails of one type
 */
data class MailMessageType(

    var id: Long = 0,

    /**
     * Name of actual mail type. Used in external API
     * to determine a type of mail that is sent.
     * Unique
     */
    val name: String,

    var description: String? = null,

    /**
     * Number of max mail send retry attempts.
     * If null, then infinite retries are possible
     */
    var maxRetriesCount: Int? = null,

    var state: MailMessageTypeState = MailMessageTypeState.ENABLED,

    // TODO html template
)

enum class MailMessageTypeState {

    ENABLED,
    DELETED,
    FORCE_DELETED,
}
