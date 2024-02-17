package io.mailit.template.core.freemarker

import freemarker.cache.TemplateNameFormat
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.mailit.template.spi.persistence.TemplateRepository

internal fun createFreemarkerTemplateProcessor(templateRepository: TemplateRepository) =
    FreemarkerTemplateProcessor(createConfiguration(RepositoryTemplateLoader(templateRepository)))

private fun createConfiguration(repositoryTemplateLoader: RepositoryTemplateLoader) =
    Configuration(Configuration.VERSION_2_3_31).apply {
        defaultEncoding = "UTF-8"
        templateLoader = repositoryTemplateLoader
        templateNameFormat = TemplateNameFormat.DEFAULT_2_4_0
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }
