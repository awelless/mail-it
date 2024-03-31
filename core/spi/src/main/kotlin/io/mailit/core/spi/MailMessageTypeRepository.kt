package io.mailit.core.spi

import io.mailit.core.model.MailMessageType
import io.mailit.core.model.Slice
import io.mailit.value.MailTypeId
import io.mailit.value.MailTypeState
import java.time.Instant

interface MailMessageTypeRepository {

    suspend fun findById(id: MailTypeId): MailMessageType?

    suspend fun findByName(name: String): MailMessageType?

    suspend fun findAllSliced(page: Int, size: Int): Slice<MailMessageType>

    suspend fun create(mailMessageType: MailMessageType): MailMessageType

    suspend fun update(mailMessageType: MailMessageType): MailMessageType

    suspend fun updateState(id: MailTypeId, state: MailTypeState, updatedAt: Instant): Int
}
