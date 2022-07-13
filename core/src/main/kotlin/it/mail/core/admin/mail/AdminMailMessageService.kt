package it.mail.core.admin.mail

import it.mail.core.model.MailMessage
import it.mail.core.model.Slice
import it.mail.persistence.api.MailMessageRepository

class AdminMailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)
}
