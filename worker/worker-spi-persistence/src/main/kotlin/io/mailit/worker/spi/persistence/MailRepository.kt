package io.mailit.worker.spi.persistence

import io.mailit.value.EmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.value.MailTypeContent
import io.mailit.value.MailTypeId
import io.mailit.value.MailTypeState
import io.mailit.value.TemplateEngine
import java.time.Instant

interface MailRepository {

    suspend fun findForSending(id: MailId): ReadPersistenceMail?

    suspend fun create(mail: WritePersistenceMail): Result<Unit>

    suspend fun update(mail: WritePersistenceMail): Result<Unit>
}

data class ReadPersistenceMail(
    val id: MailId,
    val mailType: MailType,

    val text: String?,
    val data: Map<String, Any>?,
    val subject: String?,
    val emailFrom: EmailAddress?,
    val emailTo: EmailAddress,

    val createdAt: Instant,
    var sendingStartedAt: Instant?,
    var sentAt: Instant?,

    var state: MailState,
    var failedCount: Int,

    val deduplicationId: String?,
) {
    data class MailType(
        val id: MailTypeId,
        val content: MailTypeContent,
        val state: MailTypeState,
        val maxRetriesCount: Int?,
        /**
         * For [MailTypeContent.HTML] only.
         */
        val templateEngine: TemplateEngine?,
    )
}

data class WritePersistenceMail(
    val id: MailId,
    val mailTypeId: MailTypeId,

    val text: String?,
    val data: Map<String, Any>?,
    val subject: String?,
    val emailFrom: EmailAddress?,
    val emailTo: EmailAddress,

    val createdAt: Instant,
    var sendingStartedAt: Instant?,
    var sentAt: Instant?,

    var state: MailState,
    var failedCount: Int,

    val deduplicationId: String?,
)
