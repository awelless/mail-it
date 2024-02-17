package io.mailit.template.core.freemarker

import freemarker.template.Configuration
import java.io.StringWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FreemarkerTemplateProcessor(
    private val configuration: Configuration,
) {
    suspend fun process(mailTypeId: Long, data: Map<String, Any?>) = Result.runCatching {
        val template = withContext(Dispatchers.IO) { configuration.getTemplate(mailTypeId.toString()) }

        with(StringWriter()) {
            template.process(data, this)
            toString()
        }
    }
}
