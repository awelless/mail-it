package it.mail.core.service.quarkus.mailing

import io.quarkus.mailer.Mail
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.coroutines.awaitSuspending

/**
 * Kotlin coroutine wrapper for [ReactiveMailer]
 */
class QuarkusMailSender(
    private val mailer: ReactiveMailer,
) {
    suspend fun send(mail: Mail) {
        mailer.send(mail).awaitSuspending()
    }
}
