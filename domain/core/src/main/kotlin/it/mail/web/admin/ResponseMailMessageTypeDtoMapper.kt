package it.mail.web.admin

import it.mail.domain.core.admin.type.MailMessageContentType
import it.mail.domain.core.admin.type.MailMessageContentType.HTML
import it.mail.domain.core.admin.type.MailMessageContentType.PLAIN_TEXT
import it.mail.domain.model.HtmlMailMessageType
import it.mail.domain.model.MailMessageType
import it.mail.domain.model.PlainTextMailMessageType
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
