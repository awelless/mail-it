package it.mail.web.admin

import it.mail.core.admin.mail.AdminMailMessageService
import it.mail.core.model.MailMessage
import it.mail.core.model.Slice
import it.mail.web.DEFAULT_PAGE
import it.mail.web.DEFAULT_SIZE
import it.mail.web.PAGE_PARAM
import it.mail.web.SIZE_PARAM
import it.mail.web.dto.AdminSlicedMailDto
import it.mail.web.dto.IdNameDto
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

@Path("/admin/mails")
class MailMessageResource(
    private val mailMessageService: AdminMailMessageService,
) {

    @GET
    suspend fun getAllSliced(
        @QueryParam(PAGE_PARAM) page: Int?,
        @QueryParam(SIZE_PARAM) size: Int?,
    ): Slice<AdminSlicedMailDto> {

        val slice = mailMessageService.getAllSliced(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        return slice.map { it.toDto() }
    }

    private fun MailMessage.toDto() =
        AdminSlicedMailDto(
            id = id,
            emailFrom = emailFrom,
            emailTo = emailTo,
            type = IdNameDto(type.id, type.name),
            createdAt = createdAt,
            sendingStartedAt = sendingStartedAt,
            sentAt = sentAt,
            status = status,
            failedCount = failedCount,
        )
}
