package io.mailit.admin.console.http

import io.mailit.admin.console.http.dto.AdminSlicedMailDto
import io.mailit.admin.console.http.dto.IdNameDto
import io.mailit.admin.console.security.Roles.ADMIN
import io.mailit.core.admin.api.mail.MailMessageService
import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam

@Path("/api/admin/mails")
@RolesAllowed(ADMIN)
class MailMessageResource(
    private val mailMessageService: MailMessageService,
) {

    @GET
    suspend fun getAllSliced(
        @QueryParam(PAGE_PARAM) page: Int?,
        @QueryParam(SIZE_PARAM) size: Int?,
    ): Slice<AdminSlicedMailDto> {
        val slice = mailMessageService.getAllSliced(normalizePage(page), normalizeSize(size))
        return slice.map { it.toDto() }
    }

    private fun MailMessage.toDto() = AdminSlicedMailDto(
        id = id.value.toString(),
        emailFrom = emailFrom?.email,
        emailTo = emailTo.email,
        type = IdNameDto(type.id.toString(), type.name),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        status = state,
        failedCount = failedCount,
    )
}
