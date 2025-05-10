package io.mailit.core.api.scheduler

interface ResetHungMails {

    suspend operator fun invoke()
}
