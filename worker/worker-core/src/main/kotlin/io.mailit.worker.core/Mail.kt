package io.mailit.worker.core

import io.mailit.value.EmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import java.time.Instant
import mu.KLogging

internal sealed class Mail(
    val id: MailId,
    /**
     * String that's used for mails deduplication. Messages with the same [deduplicationId] are considered as duplicates.
     * Messages with `null` [deduplicationId] are considered as different.
     */
    val deduplicationId: String?,

    val subject: String?,
    /**
     * Overrides default sender email address.
     */
    val emailFrom: EmailAddress?,
    val emailTo: EmailAddress,

    open val type: MailType,

    val createdAt: Instant,
    sendingStartedAt: Instant?,
    sentAt: Instant?,

    state: MailState,

    failedCount: Int,
) {
    val typeId
        get() = type.id

    var sendingStartedAt: Instant? = sendingStartedAt
        private set
    var sentAt: Instant? = sentAt
        private set

    var state: MailState = state
        private set

    var failedCount: Int = failedCount
        private set

    fun onSuccessfulDelivery(now: Instant) {
        state = MailState.SENT
        sentAt = now

        logger.debug { "Successfully sent Mail: ${id.value}" }
    }

    fun onFailedDelivery() {
        if (failedCount >= type.maxRetriesCount) {
            state = MailState.FAILED
            logger.error { "Failed to send Mail: ${id.value}. Max number of retries exceeded. Marking Mail as failed" }
        } else {
            failedCount++
            state = MailState.RETRY
            logger.warn { "Failed to send Mail: ${id.value}. Scheduling for another delivery" }
        }

        sendingStartedAt = null
    }

    fun shouldBeCancelled() = type.shouldCancelMessageSending()

    fun onCancelledDelivery() {
        state = MailState.CANCELED

        logger.warn { "Cancelled sending Mail: ${id.value}" }
    }

    companion object : KLogging()
}

internal class PlainMail(
    id: MailId,
    deduplicationId: String?,
    subject: String?,
    emailFrom: EmailAddress?,
    emailTo: EmailAddress,
    override val type: PlainMailType,
    createdAt: Instant,
    sendingStartedAt: Instant?,
    sentAt: Instant?,
    state: MailState,
    failedCount: Int,

    val text: String,
) : Mail(
    id = id,
    deduplicationId = deduplicationId,
    subject = subject,
    emailFrom = emailFrom,
    emailTo = emailTo,
    type = type,
    createdAt = createdAt,
    sendingStartedAt = sendingStartedAt,
    sentAt = sentAt,
    state = state,
    failedCount = failedCount,
)

internal class HtmlMail(
    id: MailId,
    deduplicationId: String?,
    subject: String?,
    emailFrom: EmailAddress?,
    emailTo: EmailAddress,
    override val type: HtmlMailType,
    createdAt: Instant,
    sendingStartedAt: Instant?,
    sentAt: Instant?,
    state: MailState,
    failedCount: Int,

    val data: Map<String, Any>,
) : Mail(
    id = id,
    deduplicationId = deduplicationId,
    subject = subject,
    emailFrom = emailFrom,
    emailTo = emailTo,
    type = type,
    createdAt = createdAt,
    sendingStartedAt = sendingStartedAt,
    sentAt = sentAt,
    state = state,
    failedCount = failedCount,
) {
    val templateEngine
        get() = type.templateEngine
}
