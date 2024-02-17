package io.mailit.value

@JvmInline
value class MailId(val value: Long)

@JvmInline
value class EmailAddress private constructor(val email: String) {

    companion object {
        private val emailAddressRegex = Regex("[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")

        fun String.toEmailAddress() =
            if (matches(emailAddressRegex)) {
                EmailAddress(this)
            } else {
                throw IllegalArgumentException("Invalid email format")
            }
    }
}

enum class MailState {
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
     * Message sending was canceled (e.g. MailType had been force deleted).
     */
    CANCELED,
}
