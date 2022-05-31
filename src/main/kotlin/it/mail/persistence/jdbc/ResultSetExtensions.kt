package it.mail.persistence.jdbc

import it.mail.core.model.HtmlMailMessageType
import it.mail.core.model.HtmlTemplateEngine
import it.mail.core.model.MailMessage
import it.mail.core.model.MailMessageStatus
import it.mail.core.model.MailMessageType
import it.mail.core.model.MailMessageTypeState
import it.mail.core.model.PlainTextMailMessageType
import it.mail.persistence.jdbc.MailMessageContent.HTML
import it.mail.persistence.jdbc.MailMessageContent.PLAIN_TEXT
import it.mail.persistence.serialization.MailMessageDataSerializer
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

    val typeDescriptionValue = getString("mt_description")
    val typeDescription = if (wasNull()) null else typeDescriptionValue

    val typeMaxRetriesCountValue = getInt("mt_max_retries_count")
    val typeMaxRetriesCount = if (wasNull()) null else typeMaxRetriesCountValue

    val typeState = MailMessageTypeState.valueOf(getString("mt_state"))
    val contentType = MailMessageContent.valueOf(getString("mt_content_type"))

    val templateEngineValue = getString("mt_template_engine")
    val templateEngine = if (wasNull()) null else HtmlTemplateEngine.valueOf(templateEngineValue)

    val templateValue = getString("mt_template")
    val template = if (wasNull()) null else templateValue

    return when (contentType) {
        PLAIN_TEXT -> PlainTextMailMessageType(
            id = typeId,
            name = typeName,
            description = typeDescription,
            maxRetriesCount = typeMaxRetriesCount,
            state = typeState,
        )
        HTML -> HtmlMailMessageType(
            id = typeId,
            name = typeName,
            description = typeDescription,
            maxRetriesCount = typeMaxRetriesCount,
            state = typeState,
            templateEngine = templateEngine!!,
            template = template!!,
        )
    }
}

internal fun ResultSet.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong("m_mail_message_id")

    val textValue = getString("m_text")
    val text = if (wasNull()) null else textValue

    val dataBlobValue = getBlob("m_data")
    val dataBlob = if (wasNull()) null else dataBlobValue
    val dataBytes = dataBlob?.binaryStream?.use {
        it.readBytes()
    }

    val data = dataSerializer.read(dataBytes)

    val subject = getString("m_subject")
    val emailFrom = getString("m_email_from")
    val emailTo = getString("m_email_to")
    val createdAt = getObject("m_created_at", Instant::class.java)

    val sendingStartedAtValue = getObject("m_sending_started_at", Instant::class.java)
    val sendingStartedAt = if (wasNull()) null else sendingStartedAtValue

    val sentAtValue = getObject("m_sent_at", Instant::class.java)
    val sentAt = if (wasNull()) null else sentAtValue

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