package io.mailit.persistence.mysql

import io.mailit.apikey.spi.persistence.ApiKey
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageStatus
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.mysql.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.mysql.Columns.MailMessage as MailMessageCol
import io.mailit.persistence.mysql.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.mysql.MailMessageContent.HTML
import io.mailit.template.api.TemplateEngine
import io.mailit.template.spi.persistence.PersistenceTemplate
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.vertx.mutiny.sqlclient.Row
import java.time.Instant
import java.time.ZoneOffset.UTC

internal fun Row.getApiKeyFromRow() = ApiKey(
    id = getString(ApiKeyCol.ID),
    name = getString(ApiKeyCol.NAME),
    secret = getString(ApiKeyCol.SECRET),
    createdAt = getInstant(ApiKeyCol.CREATED_AT),
    expiresAt = getInstant(ApiKeyCol.EXPIRES_AT),
)

internal fun Row.getMailMessageTypeFromRow(): MailMessageType {
    val typeId = getLong(MailMessageTypeCol.ID)
    val typeName = getString(MailMessageTypeCol.NAME)

    val typeDescription = getString(MailMessageTypeCol.DESCRIPTION)

    val typeMaxRetriesCount = getInteger(MailMessageTypeCol.MAX_RETRIES_COUNT)

    val typeState = MailMessageTypeState.valueOf(getString(MailMessageTypeCol.STATE))

    val createdAt = getInstant(MailMessageTypeCol.CREATED_AT)
    val updatedAt = getInstant(MailMessageTypeCol.UPDATED_AT)

    val contentType = MailMessageContent.valueOf(getString(MailMessageTypeCol.CONTENT_TYPE))

    val templateEngine = getString(MailMessageTypeCol.TEMPLATE_ENGINE)?.let { TemplateEngine.valueOf(it) }

    val template = getBuffer(MailMessageTypeCol.TEMPLATE)?.bytes

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
            template = MailMessageTemplate.fromCompressedValue(template!!),
        )
    }
}

internal fun Row.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong(MailMessageCol.ID)

    val text = getString(MailMessageCol.TEXT)

    val data = getBuffer(MailMessageCol.DATA)?.bytes?.let { dataSerializer.read(it) }

    val subject = getString(MailMessageCol.SUBJECT)
    val emailFrom = getString(MailMessageCol.EMAIL_FROM)
    val emailTo = getString(MailMessageCol.EMAIL_TO)

    val createdAt = getInstant(MailMessageCol.CREATED_AT)
    val sendingStartedAt = getNullableInstant(MailMessageCol.SENDING_STARTED_AT)
    val sentAt = getNullableInstant(MailMessageCol.SENT_AT)

    val status = MailMessageStatus.valueOf(getString(MailMessageCol.STATUS))
    val failedCount = getInteger(MailMessageCol.FAILED_COUNT)

    val deduplicationId = getString(MailMessageCol.DEDUPLICATION_ID)

    return MailMessage(
        id = id,
        text = text,
        data = data,
        subject = subject,
        emailFrom = emailFrom?.toEmailAddress(),
        emailTo = emailTo.toEmailAddress(),
        type = getMailMessageTypeFromRow(),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        status = status,
        failedCount = failedCount,
        deduplicationId = deduplicationId,
    )
}

internal fun Row.getTemplateFromRow() = PersistenceTemplate(
    mailTypeId = getLong(MailMessageTypeCol.ID),
    templateContent = MailMessageTemplate.fromCompressedValue(getBuffer(MailMessageTypeCol.TEMPLATE).bytes).value,
    updatedAt = getInstant(MailMessageTypeCol.UPDATED_AT),
)

private fun Row.getNullableInstant(column: String) = getLocalDateTime(column)?.toInstant(UTC)
private fun Row.getInstant(column: String): Instant = getLocalDateTime(column).toInstant(UTC)
