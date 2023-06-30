package io.mailit.connector.http.security

import io.mailit.core.model.application.ApiKeyToken
import io.netty.handler.codec.http.HttpResponseStatus
import io.quarkus.security.identity.IdentityProviderManager
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.vertx.http.runtime.security.ChallengeData
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport.Type.OTHER_HEADER
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils
import io.smallrye.mutiny.Uni
import io.vertx.ext.web.RoutingContext
import jakarta.inject.Singleton
import mu.KLogging

@Singleton
internal class ApiKeyAuthenticationMechanism : HttpAuthenticationMechanism {

    override fun authenticate(context: RoutingContext, identityProviderManager: IdentityProviderManager): Uni<SecurityIdentity> {
        val apiKeyHeader = context.request().getHeader(API_KEY_HEADER) ?: return Uni.createFrom().nullItem()

        logger.debug { "Found api key header: *****" + apiKeyHeader.takeLast(LOGGED_SYMBOLS_COUNT) }

        val authenticationRequest = ApiKeyAuthenticationRequest(ApiKeyToken(apiKeyHeader))
        HttpSecurityUtils.setRoutingContextAttribute(authenticationRequest, context)
        context.put(HttpAuthenticationMechanism::class.java.name, this)

        return identityProviderManager.authenticate(authenticationRequest)
    }

    override fun getChallenge(context: RoutingContext): Uni<ChallengeData> = Uni.createFrom().item(
        ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), null, null),
    )

    override fun getCredentialTypes() = CREDENTIAL_TYPES

    override fun getCredentialTransport(context: RoutingContext): Uni<HttpCredentialTransport> = Uni.createFrom().item(CREDENTIAL_TRANSPORT)

    companion object : KLogging() {
        private const val API_KEY_HEADER = "X-Api-Key"

        private const val TYPE_TARGET = "api-key"
        private val CREDENTIAL_TRANSPORT = HttpCredentialTransport(OTHER_HEADER, TYPE_TARGET)

        private val CREDENTIAL_TYPES = setOf(ApiKeyAuthenticationRequest::class.java)

        private const val LOGGED_SYMBOLS_COUNT = 7
    }
}
