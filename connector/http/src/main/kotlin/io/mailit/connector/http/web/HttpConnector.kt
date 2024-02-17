package io.mailit.connector.http.web

import io.mailit.connector.http.security.Roles.APPLICATION
import io.mailit.core.external.api.CreateMailRequest
import io.mailit.core.external.api.MailMessageService
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED

@Path("/api/connector/mail")
@RolesAllowed(APPLICATION)
class HttpConnector(
    private val mailMessageService: MailMessageService,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    suspend fun sendMail(dto: CreateMailDto): IdDto {
        val savedMail = mailMessageService.createNewMail(dto.toRequest())
        return IdDto(savedMail.id.toString())
    }

    private fun CreateMailDto.toRequest() = CreateMailRequest(
        text = text,
        data = data,
        subject = subject,
        emailFrom = emailFrom?.toEmailAddress(),
        emailTo = emailTo.toEmailAddress(),
        mailTypeName = mailType,
        deduplicationId = deduplicationId,
    )
}
