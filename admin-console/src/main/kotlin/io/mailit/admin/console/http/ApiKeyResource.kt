package io.mailit.admin.console.http

import io.mailit.admin.console.security.Roles.ADMIN
import io.mailit.apikey.api.ApiKeyCrud
import io.mailit.apikey.api.CreateApiKeyCommand
import io.quarkus.runtime.annotations.RegisterForReflection
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
    private val apiKeyCrud: ApiKeyCrud,
) {

    @GET
    suspend fun getAll() = apiKeyCrud.getAll()

    @ResponseStatus(CREATED)
    @POST
    suspend fun generate(createDto: CreateApiKeyDto): ApiKeyTokenDto {
        val command = CreateApiKeyCommand(
            name = createDto.name,
            expiration = createDto.expirationDays.days,
        )

        val token = apiKeyCrud.generate(command).getOrThrow()

        return ApiKeyTokenDto(token)
    }

    @ResponseStatus(NO_CONTENT)
    @DELETE
    @Path("/{id}")
    suspend fun delete(@PathParam("id") id: String) = apiKeyCrud.delete(id)
}

data class CreateApiKeyDto(
    val name: String,
    val expirationDays: Int,
)

@RegisterForReflection
data class ApiKeyTokenDto(val token: String)
