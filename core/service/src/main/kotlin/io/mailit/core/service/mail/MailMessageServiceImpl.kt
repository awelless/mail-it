package io.mailit.core.service.mail

import io.mailit.core.admin.api.mail.MailMessageService
import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice
import io.mailit.core.spi.MailMessageRepository
import mu.KLogging

class MailMessageServiceImpl(
    private val mailMessageRepository: MailMessageRepository,
) : MailMessageService {

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage> =
        mailMessageRepository.findAllSlicedDescendingIdSorted(page, size)

    companion object : KLogging()
}
