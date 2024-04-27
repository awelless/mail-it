package io.mailit.worker.core

import io.mailit.value.MailTypeContent
import io.mailit.worker.spi.persistence.ReadPersistenceMail
import io.mailit.worker.spi.persistence.WritePersistenceMail

internal fun ReadPersistenceMail.toDomainModel() =
    when (mailType.content) {
        MailTypeContent.PLAIN_TEXT -> toPlainMail()
        MailTypeContent.HTML -> toHtmlMail()
    }

private fun ReadPersistenceMail.toPlainMail() = PlainMail(
    id = id,
    deduplicationId = deduplicationId,
    subject = subject,
    emailFrom = emailFrom,
    emailTo = emailTo,
    type = mailType.toPlainType(),
    createdAt = createdAt,
    sendingStartedAt = sendingStartedAt,
    sentAt = sentAt,
    state = state,
    failedCount = failedCount,
    text = text ?: throw IllegalArgumentException("Plain Mail: ${id.value} doesn't contain text"),
)

private fun ReadPersistenceMail.MailType.toPlainType() = PlainMailType(
    id = id,
    state = state,
    maxRetriesCount = maxRetriesCount,
)

private fun ReadPersistenceMail.toHtmlMail() = HtmlMail(
    id = id,
    deduplicationId = deduplicationId,
    subject = subject,
    emailFrom = emailFrom,
    emailTo = emailTo,
    type = mailType.toHtmlType(),
    createdAt = createdAt,
    sendingStartedAt = sendingStartedAt,
    sentAt = sentAt,
    state = state,
    failedCount = failedCount,
    data = data ?: throw IllegalArgumentException("Html Mail: ${id.value} doesn't contain data"),
)

private fun ReadPersistenceMail.MailType.toHtmlType() = HtmlMailType(
    id = id,
    state = state,
    maxRetriesCount = maxRetriesCount,
    templateEngine = templateEngine ?: throw IllegalArgumentException("Html Mail Type: ${id.value} doesn't contain template engine"),
)

internal fun Mail.toPersistenceModel() = WritePersistenceMail(
    id = id,
    mailTypeId = typeId,
    text = (this as? PlainMail)?.text,
    data = (this as? HtmlMail)?.data,
    subject = subject,
    emailFrom = emailFrom,
    emailTo = emailTo,
    createdAt = createdAt,
    sendingStartedAt = sendingStartedAt,
    sentAt = sentAt,
    state = state,
    failedCount = failedCount,
    deduplicationId = deduplicationId,
)
