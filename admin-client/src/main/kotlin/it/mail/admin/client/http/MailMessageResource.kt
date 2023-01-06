package it.mail.admin.client.http

import it.mail.admin.client.http.dto.AdminSlicedMailDto
import it.mail.admin.client.http.dto.IdNameDto
import it.mail.admin.client.security.Roles.ADMIN
import it.mail.core.admin.api.mail.AdminMailMessageService
import it.mail.core.model.MailMessage
import it.mail.core.model.Slice
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

@Path("/api/admin/mails")
@RolesAllowed(ADMIN)
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

    private fun MailMessage.toDto() = AdminSlicedMailDto(
        id = id.toString(),
        emailFrom = emailFrom,
        emailTo = emailTo,
        type = IdNameDto(type.id.toString(), type.name),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        status = status,
        failedCount = failedCount,
    )
}
