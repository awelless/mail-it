package io.mailit.connector.http.security

import io.mailit.core.external.api.ApiKeyService
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.vertx.UniHelper
import io.vertx.kotlin.coroutines.vertxFuture
import jakarta.inject.Singleton

@Singleton
internal class ApiKeyIdentityProvider(
    private val apiKeyService: ApiKeyService,
) : IdentityProvider<ApiKeyAuthenticationRequest> {

    override fun getRequestType() = ApiKeyAuthenticationRequest::class.java

    @Suppress("USELESS_CAST")
    override fun authenticate(request: ApiKeyAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> =
        UniHelper.toUni(vertxFuture { apiKeyService.validate(request.token) })
            .onItem().transform { AuthenticatedApplication(it) as SecurityIdentity }
            .onFailure().transform { AuthenticationFailedException("Api Key is invalid") }
}
