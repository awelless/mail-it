package io.mailit.admin.console.security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.credential.Credential
import io.quarkus.security.credential.PasswordCredential
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.smallrye.mutiny.Uni
import java.security.Permission
import java.security.Principal
import javax.inject.Singleton

@Singleton
class EnvVarsIdentityProvider(private val userCredentials: UserCredentials) : IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private val authenticatedUser = AuthenticatedUser(
        username = userCredentials.username,
        password = userCredentials.password,
    )

    override fun getRequestType() = REQUEST_CLASS

    override fun authenticate(request: UsernamePasswordAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> =
        if (userCredentials.areValid(request.username, request.password.password)) {
            Uni.createFrom().item(authenticatedUser)
        } else {
            Uni.createFrom().failure(AuthenticationFailedException("Bad credentials"))
        }

    companion object {
        private val REQUEST_CLASS = UsernamePasswordAuthenticationRequest::class.java
    }
}

class AuthenticatedUser(
    private val username: String,
    private val password: CharArray,
) : SecurityIdentity {

    override fun getPrincipal() = Principal { username }

    override fun isAnonymous() = false

    override fun getRoles() = setOf(Roles.ADMIN)

    override fun hasRole(role: String?) = roles.contains(role)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Credential?> getCredential(credentialType: Class<T>) = credentials
        .filter { credentialType.isAssignableFrom(it.javaClass) }
        .map { it as T }
        .firstOrNull()

    override fun getCredentials() = setOf(PasswordCredential(password))

    override fun <T> getAttribute(name: String?): T? = null

    override fun getAttributes() = emptyMap<String, String>()

    override fun checkPermission(permission: Permission?): Uni<Boolean> = Uni.createFrom().item(true)
}
