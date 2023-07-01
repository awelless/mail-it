package io.mailit.core.service.mail.sending.templates

open class TemplateProcessingException(message: String) : Exception(message)

class InvalidTemplateEngineException(message: String) : TemplateProcessingException(message)
