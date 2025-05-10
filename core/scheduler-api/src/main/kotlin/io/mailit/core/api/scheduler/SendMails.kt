package io.mailit.core.api.scheduler

interface SendMails {

    suspend operator fun invoke()
}
