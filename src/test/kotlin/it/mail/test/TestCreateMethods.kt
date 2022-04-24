package it.mail.test

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private val COUNTER = AtomicLong()

fun createMailMessageType(): MailMessageType {
    return MailMessageType(
        id = COUNTER.incrementAndGet(),
        name = "DEFAULT",
        description = "Some description",
        maxRetriesCount = 111,
    )
}

fun createMailMessage(messageType: MailMessageType): MailMessage {
    return MailMessage(
        id = COUNTER.incrementAndGet(),
        text = "text",
        subject = null,
        emailFrom = "email@from.com",
        emailTo = "email@to.com",
        type = messageType,
        createdAt = Instant.now(),
        status = MailMessageStatus.PENDING,
    )
}
