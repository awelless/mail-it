package io.mailit.worker.quarkus

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.worker.context.WorkerContext
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.MailTypeRepository
import jakarta.inject.Singleton
import java.time.Clock

class WorkerQuarkusContext(
    idGenerator: IdGenerator,
    mailRepository: MailRepository,
    mailTypeRepository: MailTypeRepository,
) {
    private val context = WorkerContext.create(Clock.systemUTC(), idGenerator, mailRepository, mailTypeRepository)

    @Singleton
    fun createMail() = context.createMail
}
