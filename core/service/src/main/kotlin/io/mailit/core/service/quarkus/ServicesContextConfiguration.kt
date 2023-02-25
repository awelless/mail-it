package io.mailit.core.service.quarkus

import io.mailit.core.model.MailMessageType
import io.mailit.core.service.admin.mail.AdminMailMessageServiceImpl
import io.mailit.core.service.admin.type.HtmlMailMessageTypeFactory
import io.mailit.core.service.admin.type.HtmlMailMessageTypeStateUpdater
import io.mailit.core.service.admin.type.MailMessageTypeFactory
import io.mailit.core.service.admin.type.MailMessageTypeFactoryManager
import io.mailit.core.service.admin.type.MailMessageTypeServiceImpl
import io.mailit.core.service.admin.type.MailMessageTypeStateUpdater
import io.mailit.core.service.admin.type.MailMessageTypeStateUpdaterManager
import io.mailit.core.service.admin.type.PlainTextMailMessageTypeFactory
import io.mailit.core.service.admin.type.PlainTextMailMessageTypeStateUpdater
import io.mailit.core.service.external.ExternalMailMessageServiceImpl
import io.mailit.core.service.id.DistributedIdGenerator
import io.mailit.core.service.id.IdGenerator
import io.mailit.core.service.id.InstanceIdProvider
import io.mailit.core.service.mailing.HungMailsResetManager
import io.mailit.core.service.mailing.MailMessageService
import io.mailit.core.service.mailing.MailSender
import io.mailit.core.service.mailing.SendMailMessageService
import io.mailit.core.service.mailing.UnsentMailProcessor
import io.mailit.core.service.mailing.templates.TemplateProcessor
import io.mailit.core.service.mailing.templates.TemplateProcessorManager
import io.mailit.core.service.mailing.templates.freemarker.Configuration
import io.mailit.core.service.mailing.templates.freemarker.FreemarkerTemplateProcessor
import io.mailit.core.service.mailing.templates.freemarker.RepositoryTemplateLoader
import io.mailit.core.service.mailing.templates.none.NoneTemplateProcessor
import io.mailit.core.service.quarkus.mailing.MailFactory
import io.mailit.core.service.quarkus.mailing.MailSenderImpl
import io.mailit.core.service.quarkus.mailing.QuarkusMailSender
import io.mailit.core.spi.MailMessageRepository
import io.mailit.core.spi.MailMessageTypeRepository
import io.quarkus.mailer.reactive.ReactiveMailer
import javax.inject.Singleton

class AdminServicesContextConfiguration {

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
    fun adminMailMessageService(mailMessageRepository: MailMessageRepository) = AdminMailMessageServiceImpl(mailMessageRepository)
}

class ExternalServicesContextConfiguration {

    @Singleton
    fun externalMailMessageService(
        idGenerator: IdGenerator,
        mailMessageRepository: MailMessageRepository,
        mailMessageTypeRepository: MailMessageTypeRepository,
    ) = ExternalMailMessageServiceImpl(idGenerator, mailMessageRepository, mailMessageTypeRepository)
}

class MailingContextConfiguration {

    @Singleton
    fun mailMessageService(mailMessageRepository: MailMessageRepository) = MailMessageService(mailMessageRepository)

    @Singleton
    fun mailSender(
        mailFactory: MailFactory,
        mailSender: QuarkusMailSender,
    ) = MailSenderImpl(mailFactory, mailSender)

    @Singleton
    fun quarkusMailSender(mailer: ReactiveMailer) = QuarkusMailSender(mailer)

    @Singleton
    fun sendMailMessageService(
        mailSender: MailSender,
        mailMessageService: MailMessageService,
    ) = SendMailMessageService(mailSender, mailMessageService)

    @Singleton
    fun hungMailsResetManager(mailMessageService: MailMessageService) = HungMailsResetManager(mailMessageService)

    @Singleton
    fun unsentMailProcessor(
        mailMessageService: MailMessageService,
        sendService: SendMailMessageService,
    ) = UnsentMailProcessor(mailMessageService, sendService)

    @Singleton
    fun mailFactory(templateProcessor: TemplateProcessor) = MailFactory(templateProcessor)

    @Singleton
    fun templateProcessorManager(mailMessageTypeRepository: MailMessageTypeRepository) = TemplateProcessorManager(
        NoneTemplateProcessor(),
        FreemarkerTemplateProcessor(mailMessageTypeRepository),
    )

    private fun FreemarkerTemplateProcessor(mailMessageTypeRepository: MailMessageTypeRepository): FreemarkerTemplateProcessor {
        val templateLoader = RepositoryTemplateLoader(mailMessageTypeRepository)
        val configuration = Configuration(templateLoader)

        return FreemarkerTemplateProcessor(configuration)
    }
}

class IdGeneratorConfiguration {

    @Singleton
    fun idGenerator(instanceIdProvider: InstanceIdProvider) = DistributedIdGenerator(instanceIdProvider)

    // instanceIdProvider is constant
    // should be replaced with a real implementation to scale horizontally
    @Singleton
    fun instanceIdProvider() = InstanceIdProvider { 1 }
}
