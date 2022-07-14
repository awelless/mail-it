package it.mail.admin.client.http

import it.mail.admin.client.http.dto.PagedMailMessageTypeResponseDto
import it.mail.admin.client.http.dto.SingleMailMessageTypeResponseDto
import it.mail.domain.admin.api.type.MailMessageContentType
import it.mail.domain.admin.api.type.MailMessageContentType.HTML
import it.mail.domain.admin.api.type.MailMessageContentType.PLAIN_TEXT
import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.MailMessageType
import it.mail.domain.model.PlainTextMailMessageType
import javax.inject.Singleton

@Singleton
class ResponseMailMessageTypeDtoMapper {

    fun toSingleDto(mailType: MailMessageType) =
        SingleMailMessageTypeResponseDto(
            id = mailType.id,
            name = mailType.name,
            description = mailType.description,
            maxRetriesCount = mailType.maxRetriesCount,
            contentType = mailType.contentType,
            templateEngine = (mailType as? HtmlMailMessageType)?.templateEngine,
            template = (mailType as? HtmlMailMessageType)?.template,
        )

    fun toPagedDto(mailType: MailMessageType) =
        PagedMailMessageTypeResponseDto(
            id = mailType.id,
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
