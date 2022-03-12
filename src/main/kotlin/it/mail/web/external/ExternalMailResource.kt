package it.mail.web.external

import it.mail.service.external.MailMessageService
import it.mail.web.dto.CreateMailDto
import it.mail.web.dto.IdDto
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/external/mail")
class ExternalMailResource(
    private val mailMessageService: MailMessageService,
) {

    @ResponseStatus(ACCEPTED)
    @POST
    fun sendMail(@Valid dto: CreateMailDto): IdDto {
        val savedMail = mailMessageService.createNewMail(dto.text!!, dto.subject, dto.from!!, dto.to!!, dto.type!!)
        return IdDto(savedMail.externalId)
    }
}
