package io.mailit.admin.console.http

import io.mailit.admin.console.http.dto.ApiKeyTokenDto
import io.mailit.admin.console.http.dto.CreateApiKeyDto
import io.mailit.admin.console.http.dto.toDto
import io.mailit.admin.console.security.Roles.ADMIN
import io.mailit.core.admin.api.ApiKeyService
import io.mailit.core.admin.api.CreateApiKeyCommand
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import kotlin.time.Duration.Companion.days
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT

@Path("/api/admin/api-keys")
@RolesAllowed(ADMIN)
class ApiKeyResource(
    private val apiKeyService: ApiKeyService,
) {

    @GET
    suspend fun getAll() =
        apiKeyService.getAll().map { it.toDto() }

    @ResponseStatus(CREATED)
    @POST
    suspend fun generate(createDto: CreateApiKeyDto): ApiKeyTokenDto {
        val command = CreateApiKeyCommand(
            name = createDto.name,
            expiration = createDto.expirationDays.days,
        )

        val token = apiKeyService.generate(command)

        return ApiKeyTokenDto(token.value)
    }

    @ResponseStatus(NO_CONTENT)
    @DELETE
    @Path("/{id}")
    suspend fun delete(@PathParam("id") id: String) =
        apiKeyService.delete(id)
}
