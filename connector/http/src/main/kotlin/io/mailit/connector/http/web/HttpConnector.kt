package io.mailit.connector.http.web

import io.mailit.connector.http.security.Roles.APPLICATION
import io.mailit.core.external.api.CreateMailCommand
import io.mailit.core.external.api.MailMessageService
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
    suspend fun sendMail(command: CreateMailCommand): IdDto {
        val savedMail = mailMessageService.createNewMail(command)
        return IdDto(savedMail.id.toString())
    }
}