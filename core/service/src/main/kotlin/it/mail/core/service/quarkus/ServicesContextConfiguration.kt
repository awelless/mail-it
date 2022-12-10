package it.mail.core.service.quarkus

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.core.model.MailMessageType
import it.mail.core.service.admin.mail.AdminMailMessageServiceImpl
import it.mail.core.service.admin.type.HtmlMailMessageTypeFactory
import it.mail.core.service.admin.type.HtmlMailMessageTypeStateUpdater
import it.mail.core.service.admin.type.MailMessageTypeFactory
import it.mail.core.service.admin.type.MailMessageTypeFactoryManager
import it.mail.core.service.admin.type.MailMessageTypeServiceImpl
import it.mail.core.service.admin.type.MailMessageTypeStateUpdater
import it.mail.core.service.admin.type.MailMessageTypeStateUpdaterManager
import it.mail.core.service.admin.type.PlainTextMailMessageTypeFactory
import it.mail.core.service.admin.type.PlainTextMailMessageTypeStateUpdater
import it.mail.core.service.external.ExternalMailMessageServiceImpl
import it.mail.core.service.mailing.HungMailsResetManager
import it.mail.core.service.mailing.MailMessageService
import it.mail.core.service.mailing.MailSender
import it.mail.core.service.mailing.SendMailMessageService
import it.mail.core.service.mailing.UnsentMailProcessor
import it.mail.core.service.mailing.templates.TemplateProcessor
import it.mail.core.service.mailing.templates.TemplateProcessorManager
import it.mail.core.service.mailing.templates.freemarker.Configuration
import it.mail.core.service.mailing.templates.freemarker.FreemarkerTemplateProcessor
import it.mail.core.service.mailing.templates.freemarker.RepositoryTemplateLoader
import it.mail.core.service.mailing.templates.none.NoneTemplateProcessor
import it.mail.core.service.quarkus.mailing.MailFactory
import it.mail.core.service.quarkus.mailing.MailSenderImpl
import it.mail.core.service.quarkus.mailing.QuarkusMailSender
import it.mail.core.spi.MailMessageRepository
import it.mail.core.spi.MailMessageTypeRepository
import javax.inject.Singleton

class AdminServicesContextConfiguration {

    @Singleton
    fun mailMessageTypeFactory() = MailMessageTypeFactoryManager(
        PlainTextMailMessageTypeFactory(),
        HtmlMailMessageTypeFactory(),
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
        mailMessageRepository: MailMessageRepository,
        mailMessageTypeRepository: MailMessageTypeRepository,
    ) = ExternalMailMessageServiceImpl(mailMessageRepository, mailMessageTypeRepository)
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
