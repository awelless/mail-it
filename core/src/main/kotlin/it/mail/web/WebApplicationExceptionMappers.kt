package it.mail.web

import com.fasterxml.jackson.databind.JsonMappingException
import it.mail.core.NotFoundException
import it.mail.core.ValidationException
import mu.KLogging
import javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.Status.NOT_FOUND
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ValidationExceptionMapper : ExceptionMapper<ValidationException> {

    override fun toResponse(exception: ValidationException): Response =
        Response.status(BAD_REQUEST)
            .type(TEXT_PLAIN_TYPE) // TODO some dto ?
            .entity(exception.message)
            .build()
}

@Provider
class JsonMappingExceptionMapper : ExceptionMapper<JsonMappingException> {

    override fun toResponse(exception: JsonMappingException): Response =
        Response.status(BAD_REQUEST)
            .type(TEXT_PLAIN_TYPE) // TODO some dto ?
            .entity("Invalid json passed. May be some required fields are not present") // todo some more info
            .build()
}

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {

    override fun toResponse(exception: NotFoundException): Response =
        Response.status(NOT_FOUND)
            .type(TEXT_PLAIN_TYPE) // TODO some dto ?
            .entity(exception.message)
            .build()
}

@Provider
class GeneralExceptionMapper : ExceptionMapper<Exception> {

    companion object : KLogging()

    override fun toResponse(exception: Exception): Response {
        logger.error { "Exception occurred: ${exception.message}. Cause: $exception" }

        return Response.status(INTERNAL_SERVER_ERROR)
            .type(TEXT_PLAIN_TYPE)
            .entity("Server error")
            .build()
    }
}
