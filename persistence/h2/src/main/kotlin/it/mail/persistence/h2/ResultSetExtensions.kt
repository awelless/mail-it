package it.mail.persistence.h2

import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.HtmlTemplateEngine
import it.mail.domain.model.MailMessage
import it.mail.domain.model.MailMessageStatus
import it.mail.domain.model.MailMessageType
import it.mail.domain.model.MailMessageTypeState
import it.mail.domain.model.PlainTextMailMessageType
import it.mail.persistence.common.serialization.MailMessageDataSerializer
import it.mail.persistence.h2.MailMessageContent.HTML
import java.sql.ResultSet
import java.time.Instant

/*
 * All these extensions require appropriate result set column names.
 *
 * Pattern for column name is: {entityShortName}_{columnNameSnakeCase}
 *
 * Entity shortnames:
 * | class            | entityShortName |
 * |------------------|-----------------|
 * | MailMessage      | m               |
 * | MailMessageType  | mt              |
 */

internal fun ResultSet.getMailMessageTypeFromRow(): MailMessageType {
    val typeId = getLong("mt_mail_message_type_id")
    val typeName = getString("mt_name")

    val typeDescription = getString("mt_description")

    val typeMaxRetriesCount = getNullableInt("mt_max_retries_count")

    val typeState = MailMessageTypeState.valueOf(getString("mt_state"))

    val createdAt = getObject("mt_created_at", Instant::class.java)
    val updatedAt = getObject("mt_updated_at", Instant::class.java)

    val contentType = MailMessageContent.valueOf(getString("mt_content_type"))

    val templateEngineValue = getString("mt_template_engine")
    val templateEngine = if (wasNull()) null else HtmlTemplateEngine.valueOf(templateEngineValue)

    val template = getString("mt_template")

    return when (contentType) {
        MailMessageContent.PLAIN_TEXT -> PlainTextMailMessageType(
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

internal fun ResultSet.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong("m_mail_message_id")

    val text = getString("m_text")

    val dataBlob = getBlob("m_data")
    val dataBytes = dataBlob?.binaryStream?.use {
        it.readBytes()
    }

    val data = dataSerializer.read(dataBytes)

    val subject = getString("m_subject")
    val emailFrom = getString("m_email_from")
    val emailTo = getString("m_email_to")
    val createdAt = getObject("m_created_at", Instant::class.java)

    val sendingStartedAt = getObject("m_sending_started_at", Instant::class.java)

    val sentAt = getObject("m_sent_at", Instant::class.java)

    val status = MailMessageStatus.valueOf(getString("m_status"))
    val failedCount = getInt("m_failed_count")

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

private fun ResultSet.getNullableInt(columnLabel: String): Int? {
    val value = getInt(columnLabel)
    return if (wasNull()) null else value
}
