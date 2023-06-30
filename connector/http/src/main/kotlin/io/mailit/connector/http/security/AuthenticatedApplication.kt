package io.mailit.connector.http.security

import io.mailit.core.model.application.Application
import io.quarkus.security.credential.Credential
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import java.security.Permission
import java.security.Principal

internal class AuthenticatedApplication(
    private val application: Application,
) : SecurityIdentity {

    override fun getPrincipal() = Principal { application.id.toString() }

    override fun isAnonymous() = false

    override fun getRoles() = setOf(Roles.APPLICATION)

    override fun hasRole(role: String) = roles.contains(role)

    override fun <T : Credential?> getCredential(credentialType: Class<T>?) = null

    override fun getCredentials() = emptySet<Credential>()

    override fun <T> getAttribute(name: String?): T? = null

    override fun getAttributes() = emptyMap<String, Any>()

    override fun checkPermission(permission: Permission?): Uni<Boolean> = Uni.createFrom().item(true)
}

internal object Roles {
    const val APPLICATION = "application"
}
