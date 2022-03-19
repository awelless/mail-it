package it.mail.test

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import java.time.Instant
import java.util.UUID.randomUUID
import java.util.concurrent.atomic.AtomicLong

val COUNTER = AtomicLong()

fun createMailMessageType(): MailMessageType {
    val mailType = MailMessageType(
        name = "DEFAULT",
        description = "Some description",
        maxRetriesCount = 111,
    )

    mailType.id = COUNTER.incrementAndGet()

    return mailType
}

fun createMailMessage(messageType: MailMessageType): MailMessage {
    val message = MailMessage(
        text = "text",
        subject = null,
        emailFrom = "email@from.com",
        emailTo = "email@to.com",
        externalId = randomUUID().toString(),
        type = messageType,
        createdAt = Instant.now(),
        status = MailMessageStatus.PENDING,
    )

    message.id = COUNTER.incrementAndGet()

    return message
}
