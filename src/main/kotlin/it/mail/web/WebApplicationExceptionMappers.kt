package it.mail.web

import it.mail.service.BadRequestException
import it.mail.service.NotFoundException
import it.mail.web.dto.FieldConstraintViolation
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE
import javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.NOT_FOUND
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class BadRequestExceptionMapper : ExceptionMapper<BadRequestException> {

    override fun toResponse(exception: BadRequestException): Response =
        Response.status(BAD_REQUEST)
            .type(TEXT_PLAIN_TYPE) // TODO some dto ?
            .entity(exception.message)
            .build()
}

@Provider
class ConstraintViolationExceptionMapper : ExceptionMapper<ConstraintViolationException> {

    override fun toResponse(exception: ConstraintViolationException): Response {
        val violationDtos = exception.constraintViolations
            .map { FieldConstraintViolation(getFieldName(it), it.message) }

        return Response.status(BAD_REQUEST)
            .type(APPLICATION_JSON_TYPE)
            .entity(violationDtos)
            .build()
    }

    private fun getFieldName(violation: ConstraintViolation<*>) = violation.propertyPath.last().name
}

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {

    override fun toResponse(exception: NotFoundException): Response =
        Response.status(NOT_FOUND)
            .type(TEXT_PLAIN_TYPE) // TODO some dto ?
            .entity(exception.message)
            .build()
}
