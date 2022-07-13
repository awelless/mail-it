package it.mail.domain.core.quarkus.mailing

import io.quarkus.mailer.Mail
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.coroutines.awaitSuspending
import mu.KLogging

/**
 * Kotlin coroutine wrapper for [ReactiveMailer]
 */
class QuarkusMailSender(
    private val mailer: ReactiveMailer,
) {
    companion object : KLogging()

    suspend fun send(mail: Mail) {
        mailer.send(mail).awaitSuspending()
    }
}
