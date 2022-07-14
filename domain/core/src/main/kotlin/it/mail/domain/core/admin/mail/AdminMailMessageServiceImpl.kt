package it.mail.domain.core.admin.mail

import it.mail.domain.admin.api.mail.AdminMailMessageService
import it.mail.domain.model.MailMessage
import it.mail.domain.model.Slice
import it.mail.persistence.api.MailMessageRepository

class AdminMailMessageServiceImpl(
    private val mailMessageRepository: MailMessageRepository,
) : AdminMailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)
}
