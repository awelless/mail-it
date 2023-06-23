package io.mailit.persistence.postgresql

import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.HtmlTemplateEngine
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.core.model.application.ApiKey
import io.mailit.core.model.application.Application
import io.mailit.core.model.application.ApplicationState
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.postgresql.MailMessageContent.HTML
import io.vertx.mutiny.sqlclient.Row
import java.time.Instant
import java.time.ZoneOffset.UTC

/*
 * All these extensions require appropriate column names.
 *
 * Pattern for column name is: {entityShortName}_{columnNameSnakeCase}
 *
 * Entity shortnames:
 * | class            | entityShortName |
 * |------------------|-----------------|
 * | Application      | app             |
 * | MailMessage      | m               |
 * | MailMessageType  | mt              |
 */

internal fun Row.getApplicationFromRow() = Application(
    id = getLong("app_application_id"),
    name = getString("app_name"),
    state = ApplicationState.valueOf(getString("app_state")),
)

internal fun Row.getApiKeyFromRow() = ApiKey(
    id = getString("api_api_key_id"),
    name = getString("api_name"),
    secret = getString("api_secret"),
    application = getApplicationFromRow(),
    createdAt = getInstant("api_created_at"),
    expiresAt = getInstant("api_expires_at"),
)

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

internal fun Row.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong("m_mail_message_id")

    val text = getString("m_text")

    val data = getBuffer("m_data")?.bytes?.let { dataSerializer.read(it) }

    val subject = getString("m_subject")
    val emailFrom = getString("m_email_from")
    val emailTo = getString("m_email_to")

    val createdAt = getInstant("m_created_at")
    val sendingStartedAt = getNullableInstant("m_sending_started_at")
    val sentAt = getNullableInstant("m_sent_at")

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

private fun Row.getNullableInstant(column: String) = getLocalDateTime(column)?.toInstant(UTC)
private fun Row.getInstant(column: String): Instant = getLocalDateTime(column).toInstant(UTC)
