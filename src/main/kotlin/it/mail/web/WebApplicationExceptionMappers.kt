package it.mail.web

import it.mail.web.dto.FieldConstraintViolation
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ConstraintViolationExceptionMapper: ExceptionMapper<ConstraintViolationException> {

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