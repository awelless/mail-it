package io.mailit.connector.http.security

import io.quarkus.security.identity.request.BaseAuthenticationRequest

internal class ApiKeyAuthenticationRequest(val token: String) : BaseAuthenticationRequest()
