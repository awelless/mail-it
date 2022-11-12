package it.mail.core.service.mailing.templates

open class TemplateProcessingException(message: String) : Exception(message)

class InvalidTemplateEngineException(message: String) : TemplateProcessingException(message)
