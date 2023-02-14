package io.mailit.core.service.admin.mail

import io.mailit.core.admin.api.mail.AdminMailMessageService
import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository

class AdminMailMessageServiceImpl(
    private val mailMessageRepository: MailMessageRepository,
) : AdminMailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)
}
