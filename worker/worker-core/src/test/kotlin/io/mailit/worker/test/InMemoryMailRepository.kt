package io.mailit.worker.test

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.value.MailId
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.WritePersistenceMail

class InMemoryMailRepository : MailRepository {

    private val mails = mutableMapOf<MailId, WritePersistenceMail>()
    private val createdMails = mutableListOf<WritePersistenceMail>()

    override suspend fun create(mail: WritePersistenceMail) = create(mail, recordCreation = true)

    fun create(mail: WritePersistenceMail, recordCreation: Boolean) =
        if (mails.values.any { it.deduplicationId != null && it.deduplicationId == mail.deduplicationId }) {
            Result.failure(DuplicateUniqueKeyException("deduplicationId: ${mail.deduplicationId}", null))
        } else {
            mails[mail.id] = mail
            if (recordCreation) createdMails += mail
            Result.success(Unit)
        }

    fun getCreatedMails(): List<WritePersistenceMail> = createdMails
}
