package io.mailit.admin.console.http

import io.mailit.admin.console.http.dto.ApplicationDto
import io.mailit.admin.console.http.dto.toDto
import io.mailit.admin.console.security.Roles.ADMIN
import io.mailit.core.admin.api.application.ApplicationService
import io.mailit.core.admin.api.application.CreateApplicationCommand
import jakarta.annotation.security.RolesAllowed
import jakarta.json.JsonObject
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT

@Path("/api/admin/applications")
@RolesAllowed(ADMIN)
class ApplicationResource(
    private val applicationService: ApplicationService,
) {

    @GET
    @Path("/{id}")
    suspend fun getById(@PathParam("id") id: Long) = applicationService.getById(id).toDto()

    @GET
    suspend fun getAllSliced(@QueryParam(PAGE_PARAM) page: Int?, @QueryParam(SIZE_PARAM) size: Int?) =
        applicationService.getAllSliced(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE).map { it.toDto() }

    @ResponseStatus(CREATED)
    @POST
    suspend fun create(json: JsonObject): ApplicationDto {
        // having a command as a method parameter results in 400, probably the issue is with CreateApplicationCommand containing a only single attribute
        val command = CreateApplicationCommand(
            name = json.getString("name"),
        )

        return applicationService.create(command).toDto()
    }

    @ResponseStatus(NO_CONTENT)
    @DELETE
    @Path("/{id}")
    suspend fun delete(@PathParam("id") id: Long) = applicationService.delete(id)
}
