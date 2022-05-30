package it.mail.web.admin

import it.mail.domain.HtmlMailMessageType
import it.mail.domain.MailMessageType
import it.mail.domain.PlainTextMailMessageType
import it.mail.service.admin.MailMessageContentType
import it.mail.service.admin.MailMessageContentType.HTML
import it.mail.service.admin.MailMessageContentType.PLAIN_TEXT
import it.mail.web.dto.PagedMailMessageTypeResponseDto
import it.mail.web.dto.SingleMailMessageTypeResponseDto
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
