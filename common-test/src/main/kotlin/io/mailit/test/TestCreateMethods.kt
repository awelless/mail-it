package io.mailit.test

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.NONE
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
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
