package io.mailit.persistence.h2

import io.mailit.apikey.spi.persistence.ApiKey
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessage
import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import io.mailit.persistence.common.serialization.MailMessageDataSerializer
import io.mailit.persistence.h2.Columns.ApiKey as ApiKeyCol
import io.mailit.persistence.h2.Columns.MailMessage as MailMessageCol
import io.mailit.persistence.h2.Columns.MailMessageType as MailMessageTypeCol
import io.mailit.persistence.h2.MailMessageContent.HTML
import io.mailit.template.spi.persistence.PersistenceTemplate
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.value.MailTypeId
import io.mailit.value.MailTypeState
import io.mailit.value.TemplateEngine
import java.sql.ResultSet
import java.time.Instant

internal fun ResultSet.getApiKeyFromRow() = ApiKey(
    id = getString(ApiKeyCol.ID),
    name = getString(ApiKeyCol.NAME),
    secret = getString(ApiKeyCol.SECRET),
    createdAt = getInstant(ApiKeyCol.CREATED_AT),
    expiresAt = getInstant(ApiKeyCol.EXPIRES_AT),
)

internal fun ResultSet.getMailMessageTypeFromRow(): MailMessageType {
    val typeId = MailTypeId(getLong(MailMessageTypeCol.ID))
    val typeName = getString(MailMessageTypeCol.NAME)

    val typeDescription = getString(MailMessageTypeCol.DESCRIPTION)

    val typeMaxRetriesCount = getNullableInt(MailMessageTypeCol.MAX_RETRIES_COUNT)

    val typeState = MailTypeState.valueOf(getString(MailMessageTypeCol.STATE))

    val createdAt = getInstant(MailMessageTypeCol.CREATED_AT)
    val updatedAt = getInstant(MailMessageTypeCol.UPDATED_AT)

    val contentType = MailMessageContent.valueOf(getString(MailMessageTypeCol.CONTENT_TYPE))

    val templateEngineValue = getString(MailMessageTypeCol.TEMPLATE_ENGINE)
    val templateEngine = if (wasNull()) null else TemplateEngine.valueOf(templateEngineValue)

    val template = getBlob(MailMessageTypeCol.TEMPLATE)?.binaryStream?.use { it.readBytes() }

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

internal fun ResultSet.getMailMessageWithTypeFromRow(dataSerializer: MailMessageDataSerializer): MailMessage {
    val id = getLong(MailMessageCol.ID)

    val text = getString(MailMessageCol.TEXT)

    val dataBlob = getBlob(MailMessageCol.DATA)
    val dataBytes = dataBlob?.binaryStream?.use { it.readBytes() }

    val data = dataSerializer.read(dataBytes)

    val subject = getString(MailMessageCol.SUBJECT)
    val emailFrom = getString(MailMessageCol.EMAIL_FROM)
    val emailTo = getString(MailMessageCol.EMAIL_TO)

    val createdAt = getInstant(MailMessageCol.CREATED_AT)
    val sendingStartedAt = getNullableInstant(MailMessageCol.SENDING_STARTED_AT)
    val sentAt = getNullableInstant(MailMessageCol.SENT_AT)

    val status = MailState.valueOf(getString(MailMessageCol.STATE))
    val failedCount = getInt(MailMessageCol.FAILED_COUNT)

    val deduplicationId = getString(MailMessageCol.DEDUPLICATION_ID)

    return MailMessage(
        id = MailId(id),
        text = text,
        data = data,
        subject = subject,
        emailFrom = emailFrom?.toEmailAddress(),
        emailTo = emailTo.toEmailAddress(),
        type = getMailMessageTypeFromRow(),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        state = status,
        failedCount = failedCount,
        deduplicationId = deduplicationId,
    )
}

internal fun ResultSet.getTemplateFromRow() = PersistenceTemplate(
    mailTypeId = MailTypeId(getLong(MailMessageTypeCol.ID)),
    templateContent = getBlob(MailMessageTypeCol.TEMPLATE).binaryStream.use { MailMessageTemplate.fromCompressedValue(it.readBytes()).value },
    updatedAt = getInstant(MailMessageTypeCol.UPDATED_AT),
)

private fun ResultSet.getNullableInstant(columnLabel: String): Instant? = getObject(columnLabel, Instant::class.java)
private fun ResultSet.getInstant(columnLabel: String): Instant = getObject(columnLabel, Instant::class.java)

private fun ResultSet.getNullableInt(columnLabel: String): Int? {
    val value = getInt(columnLabel)
    return if (wasNull()) null else value
}
