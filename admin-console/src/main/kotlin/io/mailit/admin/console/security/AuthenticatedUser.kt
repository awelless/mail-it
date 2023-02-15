package io.mailit.admin.console.security

import io.quarkus.security.credential.Credential
import io.quarkus.security.credential.PasswordCredential
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import java.security.Permission
import java.security.Principal

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
