package io.mailit.admin.console.http

import io.mailit.value.exception.NotFoundException
import io.mailit.value.exception.ValidationException
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.BAD_REQUEST
import jakarta.ws.rs.core.Response.Status.NOT_FOUND
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import mu.KLogging

@Provider
class ValidationExceptionMapper : ExceptionMapper<ValidationException> {

    companion object : KLogging()

    override fun toResponse(exception: ValidationException): Response {
        logger.warn { "Validation error: ${exception.message}" }

        return Response.status(BAD_REQUEST)
            .type(APPLICATION_JSON)
            .entity(ErrorDto(exception.message))
            .build()
    }
}

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {

    companion object : KLogging()

    override fun toResponse(exception: NotFoundException): Response {
        logger.warn { "Not Found error: ${exception.message}" }

        return Response.status(NOT_FOUND)
            .type(APPLICATION_JSON)
            .entity(ErrorDto(exception.message))
            .build()
    }
}

data class ErrorDto(val errorMessage: String)
