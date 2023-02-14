package io.mailit.core.admin.api.type

import io.mailit.core.model.HtmlTemplateEngine
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.Slice

interface MailMessageTypeService {

    suspend fun getById(id: Long): MailMessageType

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun createNewMailType(command: CreateMailMessageTypeCommand): MailMessageType

    suspend fun updateMailType(command: UpdateMailMessageTypeCommand): MailMessageType

    suspend fun deleteMailType(id: Long, force: Boolean)
}

class CreateMailMessageTypeCommand(
    val name: String,
    val description: String? = null,
    val maxRetriesCount: Int? = null,
    val contentType: MailMessageContentType,
    val templateEngine: HtmlTemplateEngine? = null,
    val template: String? = null,
)

class UpdateMailMessageTypeCommand(
    val id: Long,
    val description: String?,
    val maxRetriesCount: Int?,
    val templateEngine: HtmlTemplateEngine? = null,
    val template: String? = null,
)

enum class MailMessageContentType {

    PLAIN_TEXT,
    HTML,
}
