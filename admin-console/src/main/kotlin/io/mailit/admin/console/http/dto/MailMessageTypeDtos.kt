package io.mailit.admin.console.http.dto

import io.mailit.core.admin.api.type.MailMessageContentType
import io.mailit.template.api.TemplateEngine
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class PagedMailMessageTypeResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
)

@RegisterForReflection
data class SingleMailMessageTypeResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val templateEngine: TemplateEngine?,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val template: String?,
)

data class MailMessageTypeCreateDto(
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val templateEngine: TemplateEngine? = null,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val template: String? = null,
)

data class MailMessageTypeUpdateDto(
    val description: String?,
    val maxRetriesCount: Int?,
    val templateEngine: TemplateEngine? = null,
    val template: String? = null,
)
