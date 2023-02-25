package io.mailit.core.model

import java.time.Instant

data class MailMessage(

    val id: Long,

    /**
     * Text that is used for [PlainTextMailMessageType]
     */
    val text: String?,

    /**
     * Data that is substituted in the template in [HtmlMailMessageType]
     */
    val data: Map<String, Any?>?,

    val subject: String?,

    /**
     * Overrides default sender email address
     */
    val emailFrom: String?,

    val emailTo: String,

    val type: MailMessageType,

    val createdAt: Instant,

    var sendingStartedAt: Instant? = null,

    var sentAt: Instant? = null,

    var status: MailMessageStatus,

    var failedCount: Int = 0,
)

enum class MailMessageStatus {

    /**
     * Just created. Available for sending
     */
    PENDING,

    /**
     * Sending failed one or several times, but has some more attempts to be sent. Available for sending
     */
    RETRY,

    /**
     * Message is being sent now
     */
    SENDING,

    /**
     * Message is successfully sent
     */
    SENT,

    /**
     * Message sending failed and all available retries were failed
     */
    FAILED,

    /**
     * Message is canceled (e.g. [MailMessageType] has been force deleted)
     */
    CANCELED,
}
