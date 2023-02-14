package io.mailit.admin.client.http

import io.mailit.core.exception.NotFoundException
import io.mailit.core.exception.ValidationException
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.NOT_FOUND
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
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
