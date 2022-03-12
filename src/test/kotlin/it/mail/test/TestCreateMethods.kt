package it.mail.test

import it.mail.domain.MailMessageType
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
