package io.mailit.admin.console.http

import io.mailit.admin.console.http.dto.MailMessageTypeCreateDto
import io.mailit.admin.console.http.dto.MailMessageTypeUpdateDto
import io.mailit.admin.console.http.dto.PagedMailMessageTypeResponseDto
import io.mailit.admin.console.http.dto.SingleMailMessageTypeResponseDto
import io.mailit.admin.console.security.Roles.ADMIN
import io.mailit.core.admin.api.type.CreateMailMessageTypeCommand
import io.mailit.core.admin.api.type.MailMessageTypeService
import io.mailit.core.admin.api.type.UpdateMailMessageTypeCommand
import io.mailit.core.model.Slice
import javax.annotation.security.RolesAllowed
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.ACCEPTED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED

@Path("/api/admin/mails/types")
@RolesAllowed(ADMIN)
class MailMessageTypeResource(
    private val mailMessageTypeService: MailMessageTypeService,
    private val mailMessageTypeDtoMapper: ResponseMailMessageTypeDtoMapper,
) {

    @GET
    @Path("/{id}")
    suspend fun getById(@PathParam("id") id: Long): SingleMailMessageTypeResponseDto {
        val mailType = mailMessageTypeService.getById(id)
        return mailMessageTypeDtoMapper.toSingleDto(mailType)
    }

    @GET
    suspend fun getAllSliced(
        @QueryParam(PAGE_PARAM) page: Int?,
        @QueryParam(SIZE_PARAM) size: Int?,
    ): Slice<PagedMailMessageTypeResponseDto> {

        val slice = mailMessageTypeService.getAllSliced(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        return slice.map { mailMessageTypeDtoMapper.toPagedDto(it) }
    }

    @ResponseStatus(CREATED)
    @POST
    suspend fun create(createDto: MailMessageTypeCreateDto): SingleMailMessageTypeResponseDto {
        val command = CreateMailMessageTypeCommand(
            name = createDto.name,
            description = createDto.description,
            maxRetriesCount = createDto.maxRetriesCount,
            contentType = createDto.contentType,
            templateEngine = createDto.templateEngine,
            template = createDto.template,
        )

        val mailType = mailMessageTypeService.createNewMailType(command)

        return mailMessageTypeDtoMapper.toSingleDto(mailType)
    }

    @PUT
    @Path("/{id}")
    suspend fun update(@PathParam("id") id: Long, updateDto: MailMessageTypeUpdateDto): SingleMailMessageTypeResponseDto {
        val command = UpdateMailMessageTypeCommand(
            id = id,
            description = updateDto.description,
            maxRetriesCount = updateDto.maxRetriesCount,
            templateEngine = updateDto.templateEngine,
            template = updateDto.template,
        )

        val mailType = mailMessageTypeService.updateMailType(command)

        return mailMessageTypeDtoMapper.toSingleDto(mailType)
    }

    @ResponseStatus(ACCEPTED)
    @DELETE
    @Path("/{id}")
    suspend fun delete(@PathParam("id") id: Long, @QueryParam("force") force: Boolean?) =
        mailMessageTypeService.deleteMailType(id, force ?: false)
}