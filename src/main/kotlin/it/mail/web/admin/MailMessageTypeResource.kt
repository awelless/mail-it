package it.mail.web.admin

import it.mail.domain.MailMessageType
import it.mail.service.admin.MailMessageTypeService
import it.mail.service.model.Slice
import it.mail.web.DEFAULT_PAGE
import it.mail.web.DEFAULT_SIZE
import it.mail.web.PAGE_PARAM
import it.mail.web.SIZE_PARAM
import it.mail.web.dto.MailMessageTypeCreateDto
import it.mail.web.dto.MailMessageTypeResponseDto
import it.mail.web.dto.MailMessageTypeUpdateDto
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam

@Path("/admin/mail/type")
class MailMessageTypeResource(
    private val mailMessageTypeService: MailMessageTypeService,
) {

    @GET
    @Path("/{id}")
    fun getById(@PathParam("id") id: Long): MailMessageTypeResponseDto {
        val mailType = mailMessageTypeService.getById(id)
        return toDto(mailType)
    }

    @GET
    fun getAllSliced(
        @QueryParam(PAGE_PARAM) page: Int?,
        @QueryParam(SIZE_PARAM) size: Int?,
    ): Slice<MailMessageTypeResponseDto> {

        val slice = mailMessageTypeService.getAllSliced(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        return slice.map { toDto(it) }
    }

    @ResponseStatus(CREATED)
    @POST
    fun create(createDto: MailMessageTypeCreateDto): MailMessageTypeResponseDto {
        val mailType = mailMessageTypeService.createNewMailType(
            name = createDto.name,
            description = createDto.description,
            maxRetriesCount = createDto.maxRetriesCount,
        )

        return toDto(mailType)
    }

    @PUT
    @Path("/{id}")
    fun update(
        @PathParam("id") id: Long,
        updateDto: MailMessageTypeUpdateDto
    ): MailMessageTypeResponseDto {

        val mailType = mailMessageTypeService.updateMailType(
            id = id,
            description = updateDto.description,
            maxRetriesCount = updateDto.maxRetriesCount,
        )

        return toDto(mailType)
    }

    private fun toDto(mailType: MailMessageType) =
        MailMessageTypeResponseDto(
            id = mailType.id,
            name = mailType.name,
            description = mailType.description,
            maxRetriesCount = mailType.maxRetriesCount,
        )
}
