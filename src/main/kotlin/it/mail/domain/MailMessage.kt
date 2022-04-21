package it.mail.domain

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.Table
import javax.persistence.Version

@Table(name = "mail_message")
@Entity
@NamedEntityGraph(
    name = "MailMessage[type]",
    attributeNodes = [NamedAttributeNode("type")],
)
class MailMessage(

    @Column(name = "text", nullable = false, updatable = false)
    val text: String,

    @Column(name = "subject", updatable = false)
    val subject: String?,

    /**
     * Overrides default sender email address
     */
    @Column(name = "email_from", updatable = false)
    val emailFrom: String?,

    @Column(name = "email_to", nullable = false, updatable = false)
    val emailTo: String,

    @Column(name = "external_id", nullable = false, updatable = false)
    val externalId: String,

    @ManyToOne
    @JoinColumn(name = "type", nullable = false, updatable = false)
    val type: MailMessageType,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "sending_started_at")
    var sendingStartedAt: Instant? = null,

    @Column(name = "sent_at")
    var sentAt: Instant? = null,

    @Column(name = "status", nullable = false)
    var status: MailMessageStatus,

    @Column(name = "failed_count", nullable = false)
    var failedCount: Int = 0,

) : BaseEntity() {

    @Version
    @Column(name = "version")
    var version: Int = 0
}

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
