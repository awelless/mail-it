package io.mailit.admin.console.security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.inject.Singleton

@Singleton
class EnvVarsIdentityProvider(private val userCredentials: UserCredentials) : IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private val authenticatedUser = AuthenticatedUser(
        username = userCredentials.username,
        password = userCredentials.password,
    )

    override fun getRequestType() = UsernamePasswordAuthenticationRequest::class.java

    override fun authenticate(request: UsernamePasswordAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> =
        if (userCredentials.areValid(request.username, request.password.password)) {
            Uni.createFrom().item(authenticatedUser)
        } else {
            Uni.createFrom().failure(AuthenticationFailedException("Bad credentials"))
        }
}
