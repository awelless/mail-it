package io.mailit.admin.console.security

import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.TrustedAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.inject.Singleton

@Singleton
class TrustedRequestIdentityProvider(userCredentials: UserCredentials) : IdentityProvider<TrustedAuthenticationRequest> {

    private val authenticatedUser = AuthenticatedUser(
        username = userCredentials.username,
        password = userCredentials.password,
    )

    override fun getRequestType() = TrustedAuthenticationRequest::class.java

    override fun authenticate(request: TrustedAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> =
        Uni.createFrom().item(authenticatedUser) // for now we have only 1 user in the system
}
