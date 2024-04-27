package io.mailit.worker.context

import io.mailit.idgenerator.api.IdGenerator
import io.mailit.template.api.TemplateProcessor
import io.mailit.worker.api.CreateMail
import io.mailit.worker.api.SendMail
import io.mailit.worker.core.CreateMailImpl
import io.mailit.worker.core.sending.SendMailImpl
import io.mailit.worker.core.sending.SendingMailFactory
import io.mailit.worker.spi.mailing.MailSender
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.MailTypeRepository
import java.time.Clock

class WorkerContext(
    val createMail: CreateMail,
    val sendMail: SendMail,
) {

    companion object {
        fun create(
            clock: Clock,
            idGenerator: IdGenerator,
            templateProcessor: TemplateProcessor,
            mailRepository: MailRepository,
            mailTypeRepository: MailTypeRepository,
            mailSender: MailSender,
        ): WorkerContext {
            return WorkerContext(
                createMail = CreateMailImpl(clock, idGenerator, mailRepository, mailTypeRepository),
                sendMail = SendMailImpl(clock, mailRepository, mailSender, SendingMailFactory(templateProcessor)),
            )
        }
    }
}
