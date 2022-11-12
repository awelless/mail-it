package it.mail.connector.http

import com.fasterxml.jackson.databind.JsonMappingException
import it.mail.core.exception.NotFoundException
import it.mail.core.exception.ValidationException
import mu.KLogging
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.Status.NOT_FOUND
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

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

@Provider
class JsonMappingExceptionMapper : ExceptionMapper<JsonMappingException> {

    companion object : KLogging()

    override fun toResponse(exception: JsonMappingException): Response {
        logger.warn { "JsonMapping error: ${exception.message}. Cause: $exception" }

        return Response.status(BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorDto("Invalid body is passed"))
            .build()
    }
}

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {

    companion object : KLogging()

    override fun toResponse(exception: NotFoundException): Response {
        logger.warn { "Not Found error: ${exception.message}" }

        return Response.status(NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorDto(exception.message))
            .build()
    }
}

@Provider
class GeneralExceptionMapper : ExceptionMapper<Exception> {

    companion object : KLogging()

    override fun toResponse(exception: Exception): Response {
        logger.error { "Exception occurred: ${exception.message}. Cause: $exception" }

        return Response.status(INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorDto("Server error"))
            .build()
    }
}

data class ErrorDto(val errorMessage: String)
