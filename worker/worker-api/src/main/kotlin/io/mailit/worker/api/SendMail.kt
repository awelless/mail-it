package io.mailit.worker.api

import io.mailit.value.MailId

interface SendMail {

    suspend operator fun invoke(id: MailId): Result<Unit>
}
