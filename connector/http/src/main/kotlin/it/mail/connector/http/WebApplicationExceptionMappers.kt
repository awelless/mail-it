package it.mail.connector.http

import it.mail.core.exception.ValidationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
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
