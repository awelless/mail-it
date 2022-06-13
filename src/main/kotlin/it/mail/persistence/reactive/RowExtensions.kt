package it.mail.persistence.reactive

import io.vertx.mutiny.sqlclient.Row
import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus
import it.mail.core.model.MailMessageType
import it.mail.core.model.MailMessageTypeState
import it.mail.core.model.PlainTextMailMessageType
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.jdbc.MailMessageContent
import it.mail.persistence.jdbc.MailMessageContent.HTML
import it.mail.persistence.jdbc.MailMessageContent.PLAIN_TEXT
import java.time.Instant

/*
 * All these extensions require appropriate column names.
 *
 * Pattern for column name is: {entityShortName}_{columnNameSnakeCase}
 *
 * Entity shortnames:
 * | class            | entityShortName |
 * |------------------|-----------------|
 * | MailMessage      | m               |
 * | MailMessageType  | mt              |
 */

internal fun Row.getMailMessageTypeFromRow(): MailMessageType {
    val typeId = getLong("mt_mail_message_type_id")
    val typeName = getString("mt_name")

    val typeDescription = getString("mt_description")

    val typeMaxRetriesCount = getInteger("mt_max_retries_count")

    val typeState = MailMessageTypeState.valueOf(getString("mt_state"))

    val createdAt = getInstant("mt_created_at")
    val updatedAt = getInstant("mt_updated_at")

    val contentType = MailMessageContent.valueOf(getString("mt_content_type"))

    val templateEngine = getString("mt_template_engine")?.let { HtmlTemplateEngine.valueOf(it) }

    val template = getString("mt_template")

    return when (contentType) {
        PLAIN_TEXT -> PlainTextMailMessageType(
            id = typeId,
            name = typeName,
            description = typeDescription,
            maxRetriesCount = typeMaxRetriesCount,
            state = typeState,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
        HTML -> HtmlMailMessageType(
            id = typeId,
            name = typeName,
            description = typeDescription,
            maxRetriesCount = typeMaxRetriesCount,
            state = typeState,
            createdAt = createdAt,
            updatedAt = updatedAt,
            templateEngine = templateEngine!!,
            template = template!!,
        )
    }
}

internal fun Row.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong("m_mail_message_id")

    val text = getString("m_text")

    val data = getBuffer("m_data")?.bytes?.let { dataSerializer.read(it) }

    val subject = getString("m_subject")
    val emailFrom = getString("m_email_from")
    val emailTo = getString("m_email_to")
    val createdAt = getInstant("m_created_at")

    val sendingStartedAt = getInstant("m_sending_started_at")

    val sentAt = getInstant("m_sent_at")

    val status = MailMessageStatus.valueOf(getString("m_status"))
    val failedCount = getInteger("m_failed_count")

    return MailMessage(
        id = id,
        text = text,
        data = data,
        subject = subject,
        emailFrom = emailFrom,
        emailTo = emailTo,
        type = getMailMessageTypeFromRow(),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        status = status,
        failedCount = failedCount,
    )
}

private fun Row.getInstant(column: String) = Instant.from(getTemporal(column))
