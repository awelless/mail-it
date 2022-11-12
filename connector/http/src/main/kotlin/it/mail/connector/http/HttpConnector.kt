package it.mail.connector.http

import it.mail.core.external.api.CreateMailCommand
import it.mail.core.external.api.ExternalMailMessageService
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/api/connector/mail")
class HttpConnector(
    private val mailMessageService: ExternalMailMessageService,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    suspend fun sendMail(command: CreateMailCommand): IdDto {
        val savedMail = mailMessageService.createNewMail(command)
        return IdDto(savedMail.id)
    }
}
