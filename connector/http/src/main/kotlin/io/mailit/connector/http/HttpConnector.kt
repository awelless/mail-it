package io.mailit.connector.http

import io.mailit.core.external.api.CreateMailCommand
import io.mailit.core.external.api.ExternalMailMessageService
import javax.ws.rs.POST
import javax.ws.rs.Path
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED

@Path("/api/connector/mail")
class HttpConnector(
    private val mailMessageService: ExternalMailMessageService,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    suspend fun sendMail(command: CreateMailCommand): IdDto {
        val savedMail = mailMessageService.createNewMail(command)
        return IdDto(savedMail.id.toString())
    }
}
