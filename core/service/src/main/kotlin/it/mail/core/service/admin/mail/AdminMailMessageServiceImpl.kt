package it.mail.core.service.admin.mail

import it.mail.core.admin.api.mail.AdminMailMessageService
import it.mail.core.model.MailMessage
import it.mail.core.model.Slice
import it.mail.core.persistence.api.MailMessageRepository

class AdminMailMessageServiceImpl(
    private val mailMessageRepository: MailMessageRepository,
) : AdminMailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)
}
