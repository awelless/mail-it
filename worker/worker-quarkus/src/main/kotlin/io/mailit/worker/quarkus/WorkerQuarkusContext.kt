package io.mailit.worker.quarkus

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.template.api.TemplateProcessor
import io.mailit.worker.context.WorkerContext
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.MailTypeRepository
import io.quarkus.mailer.reactive.ReactiveMailer
import jakarta.inject.Singleton
import java.time.Clock

class WorkerQuarkusContext(
    idGenerator: IdGenerator,
    templateProcessor: TemplateProcessor,
    mailRepository: MailRepository,
    mailTypeRepository: MailTypeRepository,
    mailer: ReactiveMailer,
) {
    private val context = WorkerContext.create(
        Clock.systemUTC(),
        idGenerator,
        templateProcessor,
        mailRepository,
        mailTypeRepository,
        QuarkusMailSender(mailer),
    )

    @Singleton
    fun createMail() = context.createMail

    @Singleton
    fun sendMail() = context.sendMail
}
