package io.mailit.core.service.quarkus

import io.mailit.core.model.MailMessageType
import io.mailit.core.service.mail.MailMessageServiceImpl
import io.mailit.core.service.mail.sending.HungMailsResetManager
import io.mailit.core.service.mail.sending.MailFactory
import io.mailit.core.service.mail.sending.MailMessageService
import io.mailit.core.service.mail.sending.SendMailMessageService
import io.mailit.core.service.mail.sending.UnsentMailProcessor
import io.mailit.core.service.mail.type.HtmlMailMessageTypeFactory
import io.mailit.core.service.mail.type.HtmlMailMessageTypeStateUpdater
import io.mailit.core.service.mail.type.MailMessageTypeFactory
import io.mailit.core.service.mail.type.MailMessageTypeFactoryManager
import io.mailit.core.service.mail.type.MailMessageTypeServiceImpl
import io.mailit.core.service.mail.type.MailMessageTypeStateUpdater
import io.mailit.core.service.mail.type.MailMessageTypeStateUpdaterManager
import io.mailit.core.service.mail.type.PlainTextMailMessageTypeFactory
import io.mailit.core.service.mail.type.PlainTextMailMessageTypeStateUpdater
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.core.spi.mailer.MailSender
import io.mailit.idgenerator.api.IdGenerator
import io.mailit.template.api.TemplateProcessor
import jakarta.inject.Singleton
import java.time.Clock

class ServicesContextConfiguration {

    @Singleton
    fun mailMessageTypeFactory(idGenerator: IdGenerator) = MailMessageTypeFactoryManager(
        PlainTextMailMessageTypeFactory(idGenerator),
        HtmlMailMessageTypeFactory(idGenerator),
    )

    @Singleton
    fun mailMessageTypeStateUpdater() = MailMessageTypeStateUpdaterManager(
        PlainTextMailMessageTypeStateUpdater(),
        HtmlMailMessageTypeStateUpdater(),
    )

    @Singleton
    fun mailMessageTypeService(
        mailMessageTypeRepository: MailMessageTypeRepository,
        mailMessageTypeFactory: MailMessageTypeFactory<MailMessageType>,
        mailMessageTypeStateUpdated: MailMessageTypeStateUpdater<MailMessageType>,
    ) = MailMessageTypeServiceImpl(mailMessageTypeRepository, mailMessageTypeFactory, mailMessageTypeStateUpdated)

    @Singleton
    fun mailMessageService(
        idGenerator: IdGenerator,
        mailMessageRepository: MailMessageRepository,
        mailMessageTypeRepository: MailMessageTypeRepository,
    ) = MailMessageServiceImpl(idGenerator, mailMessageRepository, mailMessageTypeRepository)
}

class MailingContextConfiguration {

    @Singleton
    fun mailMessageService(mailMessageRepository: MailMessageRepository) = MailMessageService(mailMessageRepository, Clock.systemUTC())

    @Singleton
    fun sendMailMessageService(
        mailFactory: MailFactory,
        mailSender: MailSender,
        mailMessageService: MailMessageService,
    ) = SendMailMessageService(mailFactory, mailSender, mailMessageService)

    @Singleton
    fun hungMailsResetManager(mailMessageService: MailMessageService) = HungMailsResetManager(mailMessageService)

    @Singleton
    fun unsentMailProcessor(
        mailMessageService: MailMessageService,
        sendService: SendMailMessageService,
    ) = UnsentMailProcessor(mailMessageService, sendService)

    @Singleton
    fun mailFactory(templateProcessor: TemplateProcessor) = MailFactory(templateProcessor)
}
