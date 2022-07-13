package it.mail.core.mailing.templates.freemarker

import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler

fun Configuration(templateLoader: TemplateLoader): Configuration {
    val configuration = Configuration(Configuration.VERSION_2_3_31)

    configuration.defaultEncoding = "UTF-8"
    configuration.templateLoader = templateLoader
    configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

    return configuration
}
