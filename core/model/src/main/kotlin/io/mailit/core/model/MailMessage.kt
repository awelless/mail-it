package io.mailit.core.model

import io.mailit.value.EmailAddress
import io.mailit.value.MailId
import java.time.Instant

data class MailMessage(

    val id: MailId,

    /**
     * Text that is used for [PlainTextMailMessageType]
     */
    val text: String?,

    /**
     * Data that is substituted in the template in [HtmlMailMessageType]
     */
    val data: Map<String, Any>?,

    val subject: String?,

    /**
     * Overrides default sender email address
     */
    val emailFrom: EmailAddress?,

    val emailTo: EmailAddress,

    val type: MailMessageType,

    val createdAt: Instant,

    var sendingStartedAt: Instant? = null,

    var sentAt: Instant? = null,

    var status: MailMessageStatus,

    var failedCount: Int = 0,

    /**
     * String that's used for mails deduplication. Messages with the same [deduplicationId] are considered as duplicates.
     * Messages with `null` [deduplicationId] are considered as different.
     */
    val deduplicationId: String?,
)

enum class MailMessageStatus {

    /**
     * Just created. Available for sending.
     */
    PENDING,

    /**
     * Sending failed one or several times, but can be retried. Available for sending.
     */
    RETRY,

    /**
     * Message is being sent right now.
     */
    SENDING,

    /**
     * Message has been sent successfully.
     */
    SENT,

    /**
     * Message sending failed and all available retries failed.
     */
    FAILED,

    /**
     * Message sending was canceled (e.g. [MailMessageType] had been force deleted).
     */
    CANCELED,
}
