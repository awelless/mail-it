package io.mailit.connector.http.web

import io.mailit.core.exception.ValidationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.BAD_REQUEST
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import mu.KLogging

@Provider
class ValidationExceptionMapper : ExceptionMapper<ValidationException> {

    companion object : KLogging()

    override fun toResponse(exception: ValidationException): Response {
        logger.warn { "Validation error: ${exception.message}" }

        return Response.status(BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorDto(exception.message))
            .build()
    }
}

data class ErrorDto(val errorMessage: String)
