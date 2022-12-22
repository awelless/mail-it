package it.mail.admin.client.http

import it.mail.admin.client.security.UserCredentials
import javax.annotation.security.PermitAll
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.UNAUTHORIZED

@Path("/api/admin")
@PermitAll
class AuthenticationResource(
    private val userCredentials: UserCredentials,
) {

    @POST
    @Path("/login")
    suspend fun login(
        @QueryParam(USERNAME_PARAM) username: String,
        @QueryParam(PASSWORD_PARAM) password: String,
    ): Response =
        if (userCredentials.areValid(username, password.toCharArray())) {
            Response.ok().build()
        } else {
            Response.status(UNAUTHORIZED).build()
        }

    companion object {
        private const val USERNAME_PARAM = "username"
        private const val PASSWORD_PARAM = "username"
    }
}
