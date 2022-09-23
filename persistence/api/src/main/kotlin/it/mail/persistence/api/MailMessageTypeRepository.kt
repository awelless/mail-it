package it.mail.persistence.api

import it.mail.domain.model.MailMessageType
import it.mail.domain.model.MailMessageTypeState
import it.mail.domain.model.Slice
import java.time.Instant

interface MailMessageTypeRepository {

    suspend fun findById(id: Long): MailMessageType?

    suspend fun findByName(name: String): MailMessageType?

    suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun create(mailMessageType: MailMessageType): MailMessageType

    suspend fun update(mailMessageType: MailMessageType): MailMessageType

    suspend fun updateState(id: Long, state: MailMessageTypeState, updatedAt: Instant): Int
}
