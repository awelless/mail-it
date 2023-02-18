package io.mailit.admin.console.http

import io.mailit.admin.console.security.Roles
import io.quarkus.security.identity.SecurityIdentity
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.Path

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
