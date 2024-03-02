package io.mailit.worker.spi.persistence

import io.mailit.value.EmailAddress
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.value.MailTypeId
import java.time.Instant

interface MailRepository {

    suspend fun create(mail: PersistenceMail): Result<Unit>
}

data class PersistenceMail(
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
