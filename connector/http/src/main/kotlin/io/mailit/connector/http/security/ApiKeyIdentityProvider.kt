package io.mailit.connector.http.security

import io.mailit.apikey.api.ApiKeyValidator
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
    private val apiKeyValidator: ApiKeyValidator,
) : IdentityProvider<ApiKeyAuthenticationRequest> {

    override fun getRequestType() = ApiKeyAuthenticationRequest::class.java

    @Suppress("USELESS_CAST")
    override fun authenticate(request: ApiKeyAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> =
        UniHelper.toUni(vertxFuture { apiKeyValidator.validate(request.token) })
            .onItem().transformToUni { apiKeyName ->
                apiKeyName.fold(
                    onSuccess = { Uni.createFrom().item(AuthenticatedApplication(it) as SecurityIdentity) },
                    onFailure = { Uni.createFrom().failure(AuthenticationFailedException("Api Key is invalid")) },
                )
            }
}
