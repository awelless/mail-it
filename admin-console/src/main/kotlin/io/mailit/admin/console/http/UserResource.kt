package io.mailit.admin.console.http

import io.mailit.admin.console.security.Roles
import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/api/admin/users")
@RolesAllowed(Roles.ADMIN)
class UserResource(
    private val securityIdentity: SecurityIdentity,
) {

    @GET
    @Path("/me")
    fun getMe() = UserDto(
        username = securityIdentity.principal.name,
    )
}

data class UserDto(val username: String)
