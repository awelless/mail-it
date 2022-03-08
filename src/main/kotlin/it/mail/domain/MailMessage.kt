package it.mail.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
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
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null
}