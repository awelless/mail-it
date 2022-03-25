package it.mail.domain

import it.mail.domain.MailMessageTypeState.ENABLED
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * Type of [MailMessage]. Contains general info for all mails of one type
 */
@Table(name = "mail_message_type")
@Entity
class MailMessageType(

    /**
     * Name of actual mail type. Used in external API
     * to determine a type of mail that is sent
     */
    @Column(name = "name", nullable = false, unique = true, updatable = false)
    val name: String,

    @Column(name = "description")
    var description: String? = null,

    /**
     * Number of max mail send retry attempts.
     * If null, then infinite retries are possible
     */
    @Column(name = "max_retries_count")
    var maxRetriesCount: Int? = null,

    @Column(name = "state", nullable = false)
    var state: MailMessageTypeState = ENABLED,

    // TODO html template

) : BaseEntity()

enum class MailMessageTypeState {

    ENABLED,
    DELETED,
    FORCE_DELETED,
}
