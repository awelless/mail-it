package io.mailit.core.admin.api.mail

import io.mailit.core.model.MailMessage
import io.mailit.core.model.Slice

interface AdminMailMessageService {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage>
}
