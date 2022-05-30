package it.mail.test

import it.mail.domain.HtmlMailMessageType
import it.mail.domain.HtmlTemplateEngine.NONE
import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import it.mail.domain.PlainTextMailMessageType
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private val COUNTER = AtomicLong()

fun createPlainMailMessageType(): PlainTextMailMessageType =
    PlainTextMailMessageType(
        id = COUNTER.incrementAndGet(),
        name = "DEFAULT",
        description = "Some description",
        maxRetriesCount = 111,
    )

fun createHtmlMailMessageType(): HtmlMailMessageType =
    HtmlMailMessageType(
        id = COUNTER.incrementAndGet(),
        name = "DEFAULT",
        description = "Some description",
        maxRetriesCount = 111,
        templateEngine = NONE,
        template = "<html></html>"
    )

fun createMailMessage(messageType: MailMessageType): MailMessage =
    MailMessage(
        id = COUNTER.incrementAndGet(),
        text = "text",
        subject = null,
        emailFrom = "email@from.com",
        emailTo = "email@to.com",
        type = messageType,
        createdAt = Instant.now(),
        status = MailMessageStatus.PENDING,
    )
