package io.mailit.test

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.template.api.TemplateEngine
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailTypeId
import java.util.concurrent.atomic.AtomicLong

private val counter = AtomicLong()

fun createPlainMailMessageType(): PlainTextMailMessageType {
    val id = counter.incrementAndGet()

    return PlainTextMailMessageType(
        id = MailTypeId(id),
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
        id = MailTypeId(id),
        name = "html-type-$id",
        description = "Some description",
        createdAt = nowWithoutNanos(),
        updatedAt = nowWithoutNanos(),
        maxRetriesCount = 111,
        templateEngine = TemplateEngine.NONE,
        template = MailMessageTemplate("<html></html>"),
    )
}

fun createMailMessage(messageType: MailMessageType): MailMessage {
    val id = counter.incrementAndGet()

    return MailMessage(
        id = MailId(id),
        text = "text",
        data = emptyMap(),
        subject = null,
        emailFrom = "email@from.com".toEmailAddress(),
        emailTo = "email@to.com".toEmailAddress(),
        type = messageType,
        createdAt = nowWithoutNanos(),
        status = MailMessageStatus.PENDING,
        deduplicationId = id.toString(),
    )
}
