package io.mailit.connector.http.web

import io.mailit.connector.http.security.Roles.APPLICATION
import io.mailit.value.EmailAddress.Companion.toEmailAddress
import io.mailit.worker.api.CreateMail
import io.mailit.worker.api.CreateMailRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED

@Path("/api/connector/mail")
@RolesAllowed(APPLICATION)
class HttpConnector(
    private val createMail: CreateMail,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    suspend fun sendMail(dto: CreateMailDto) {
        createMail(dto.toRequest()).getOrThrow()
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
