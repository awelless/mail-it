package it.mail.domain.core.admin.mail

import it.mail.domain.model.MailMessage
import it.mail.domain.model.Slice
import it.mail.persistence.api.MailMessageRepository

class AdminMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)
}
