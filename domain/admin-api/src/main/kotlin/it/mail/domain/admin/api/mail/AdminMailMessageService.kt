package it.mail.domain.admin.api.mail

import it.mail.domain.model.MailMessage
import it.mail.domain.model.Slice

interface AdminMailMessageService {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage>
}
