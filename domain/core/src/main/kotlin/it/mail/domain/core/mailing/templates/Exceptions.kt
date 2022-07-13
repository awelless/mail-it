package it.mail.domain.core.mailing.templates

open class TemplateProcessingException(message: String) : Exception(message)

class InvalidTemplateEngineException(message: String) : TemplateProcessingException(message)
