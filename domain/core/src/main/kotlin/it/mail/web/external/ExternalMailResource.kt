package it.mail.web.external

import it.mail.domain.core.external.ExternalMailMessageService
import it.mail.web.dto.CreateMailDto
import it.mail.web.dto.IdDto
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/external/mail")
class ExternalMailResource(
    private val mailMessageService: ExternalMailMessageService,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    suspend fun sendMail(dto: CreateMailDto): IdDto {
        val savedMail = mailMessageService.createNewMail(dto.text, dto.data, dto.subject, dto.from, dto.to, dto.typeId)
        return IdDto(savedMail.id)
    }
}
