package it.mail.test

import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine.NONE
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus
import it.mail.core.model.MailMessageType
import it.mail.core.model.PlainTextMailMessageType
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private val COUNTER = AtomicLong()

fun createPlainMailMessageType(): PlainTextMailMessageType =
    PlainTextMailMessageType(
        id = COUNTER.incrementAndGet(),
        name = "DEFAULT",
        description = "Some description",
        maxRetriesCount = 111,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

fun createHtmlMailMessageType(): HtmlMailMessageType =
    HtmlMailMessageType(
        id = COUNTER.incrementAndGet(),
        name = "DEFAULT",
        description = "Some description",
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        maxRetriesCount = 111,
        templateEngine = NONE,
        template = "<html></html>",
    )

fun createMailMessage(messageType: MailMessageType): MailMessage =
    MailMessage(
        id = COUNTER.incrementAndGet(),
        text = "text",
        data = emptyMap(),
        subject = null,
        emailFrom = "email@from.com",
        emailTo = "email@to.com",
        type = messageType,
        createdAt = Instant.now(),
        status = MailMessageStatus.PENDING,
    )
