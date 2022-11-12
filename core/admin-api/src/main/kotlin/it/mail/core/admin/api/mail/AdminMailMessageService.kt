package it.mail.core.admin.api.mail

import it.mail.core.model.MailMessage
import it.mail.core.model.Slice

interface AdminMailMessageService {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage>
}
