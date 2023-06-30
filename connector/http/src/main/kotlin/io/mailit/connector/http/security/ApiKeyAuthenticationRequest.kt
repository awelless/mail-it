package io.mailit.connector.http.security

import io.mailit.core.model.application.ApiKeyToken
import io.quarkus.security.identity.request.BaseAuthenticationRequest

internal class ApiKeyAuthenticationRequest(val token: ApiKeyToken) : BaseAuthenticationRequest()
