package io.mailit.core.admin.api.mail

import io.mailit.core.model.MailMessage
import io.mailit.value.Slice

interface MailMessageService {

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessage>
}
