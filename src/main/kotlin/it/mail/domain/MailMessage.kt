package it.mail.domain

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "mail_message")
@Entity
class MailMessage(

    @Column(name = "text", nullable = false, updatable = false)
    val text: String,

    @Column(name = "subject", updatable = false)
    val subject: String?,

    @Column(name = "email_from", nullable = false, updatable = false)
    val emailFrom: String,

    @Column(name = "email_to", nullable = false, updatable = false)
    val emailTo: String,

    @Column(name = "external_id", nullable = false, updatable = false)
    val externalId: String,

    @ManyToOne
    @JoinColumn(name = "type", nullable = false, updatable = false)
    val type: MailMessageType,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

) : BaseEntity()
