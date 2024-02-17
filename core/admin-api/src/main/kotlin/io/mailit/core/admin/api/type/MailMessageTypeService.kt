package io.mailit.core.admin.api.type

import io.mailit.core.model.MailMessageTemplate
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.Slice
import io.mailit.template.api.TemplateEngine
import io.mailit.value.MailTypeId

interface MailMessageTypeService {

    suspend fun getById(id: MailTypeId): MailMessageType

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun createNewMailType(command: CreateMailMessageTypeCommand): MailMessageType

    suspend fun updateMailType(command: UpdateMailMessageTypeCommand): MailMessageType

    suspend fun deleteMailType(id: MailTypeId, force: Boolean)
}

data class CreateMailMessageTypeCommand(
    val name: String,
    val description: String? = null,
    val maxRetriesCount: Int? = null,
    val contentType: MailMessageContentType,
    val templateEngine: TemplateEngine? = null,
    val template: MailMessageTemplate? = null,
)

data class UpdateMailMessageTypeCommand(
    val id: MailTypeId,
    val description: String?,
    val maxRetriesCount: Int?,
    val templateEngine: TemplateEngine? = null,
    val template: MailMessageTemplate? = null,
)

enum class MailMessageContentType {

    PLAIN_TEXT,
    HTML,
}
