package io.mailit.admin.console.http

import io.mailit.admin.console.http.dto.PagedMailMessageTypeResponseDto
import io.mailit.admin.console.http.dto.SingleMailMessageTypeResponseDto
import io.mailit.core.api.admin.type.MailMessageContentType
import io.mailit.core.api.admin.type.MailMessageContentType.HTML
import io.mailit.core.api.admin.type.MailMessageContentType.PLAIN_TEXT
import io.mailit.core.model.HtmlMailMessageType
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.PlainTextMailMessageType
import jakarta.inject.Singleton

@Singleton
class ResponseMailMessageTypeDtoMapper {

    fun toSingleDto(mailType: MailMessageType) =
        SingleMailMessageTypeResponseDto(
            id = mailType.id.value.toString(),
            name = mailType.name,
            description = mailType.description,
            maxRetriesCount = mailType.maxRetriesCount,
            contentType = mailType.contentType,
            templateEngine = (mailType as? HtmlMailMessageType)?.templateEngine,
            template = (mailType as? HtmlMailMessageType)?.template?.value,
        )

    fun toPagedDto(mailType: MailMessageType) =
        PagedMailMessageTypeResponseDto(
            id = mailType.id.value.toString(),
            name = mailType.name,
            description = mailType.description,
            maxRetriesCount = mailType.maxRetriesCount,
            contentType = mailType.contentType,
        )

    private val MailMessageType.contentType: MailMessageContentType
        get() = when (this) {
            is PlainTextMailMessageType -> PLAIN_TEXT
            is HtmlMailMessageType -> HTML
        }
}
