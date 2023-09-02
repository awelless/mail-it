package io.mailit.test

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine.NONE
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import java.util.concurrent.atomic.AtomicLong

private val counter = AtomicLong()

fun createPlainMailMessageType(): PlainTextMailMessageType {
    val id = counter.incrementAndGet()

    return PlainTextMailMessageType(
        id = id,
        name = "plain-type-$id",
        description = "Some description",
        maxRetriesCount = 111,
        createdAt = nowWithoutNanos(),
        updatedAt = nowWithoutNanos(),
    )
}

fun createHtmlMailMessageType(): HtmlMailMessageType {
    val id = counter.incrementAndGet()

    return HtmlMailMessageType(
        id = id,
        name = "html-type-$id",
        description = "Some description",
        createdAt = nowWithoutNanos(),
        updatedAt = nowWithoutNanos(),
        maxRetriesCount = 111,
        templateEngine = NONE,
        template = MailMessageTemplate("<html></html>"),
    )
}

fun createMailMessage(messageType: MailMessageType): MailMessage =
    MailMessage(
        id = counter.incrementAndGet(),
        text = "text",
        data = emptyMap(),
        subject = null,
        emailFrom = "email@from.com",
        emailTo = "email@to.com",
        type = messageType,
        createdAt = nowWithoutNanos(),
        status = MailMessageStatus.PENDING,
    )
