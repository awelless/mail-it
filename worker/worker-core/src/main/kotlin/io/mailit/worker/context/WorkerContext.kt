package io.mailit.worker.context

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.worker.api.CreateMail
import io.mailit.worker.core.CreateMailImpl
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.MailTypeRepository
import java.time.Clock

class WorkerContext(
    val createMail: CreateMail,
) {

    companion object {
        fun create(
            clock: Clock,
            idGenerator: IdGenerator,
            mailRepository: MailRepository,
            mailTypeRepository: MailTypeRepository,
        ): WorkerContext {
            return WorkerContext(
                createMail = CreateMailImpl(clock, idGenerator, mailRepository, mailTypeRepository),
            )
        }
    }
}
