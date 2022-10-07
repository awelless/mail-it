package it.mail.domain.core.quarkus

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.domain.core.admin.mail.AdminMailMessageServiceImpl
import it.mail.domain.core.admin.type.HtmlMailMessageTypeFactory
import it.mail.domain.core.admin.type.HtmlMailMessageTypeStateUpdater
import it.mail.domain.core.admin.type.MailMessageTypeFactory
import it.mail.domain.core.admin.type.MailMessageTypeFactoryManager
import it.mail.domain.core.admin.type.MailMessageTypeServiceImpl
import it.mail.domain.core.admin.type.MailMessageTypeStateUpdater
import it.mail.domain.core.admin.type.MailMessageTypeStateUpdaterManager
import it.mail.domain.core.admin.type.PlainTextMailMessageTypeFactory
import it.mail.domain.core.admin.type.PlainTextMailMessageTypeStateUpdater
import it.mail.domain.core.external.ExternalMailMessageServiceImpl
import it.mail.domain.core.mailing.HungMailsResetManager
import it.mail.domain.core.mailing.MailMessageService
import it.mail.domain.core.mailing.MailSender
import it.mail.domain.core.mailing.SendMailMessageService
import it.mail.domain.core.mailing.UnsentMailProcessor
import it.mail.domain.core.mailing.templates.TemplateProcessor
import it.mail.domain.core.mailing.templates.TemplateProcessorManager
import it.mail.domain.core.mailing.templates.freemarker.Configuration
import it.mail.domain.core.mailing.templates.freemarker.FreemarkerTemplateProcessor
import it.mail.domain.core.mailing.templates.freemarker.RepositoryTemplateLoader
import it.mail.domain.core.mailing.templates.none.NoneTemplateProcessor
import it.mail.domain.core.quarkus.mailing.HtmlMailFactory
import it.mail.domain.core.quarkus.mailing.MailFactory
import it.mail.domain.core.quarkus.mailing.MailFactoryManager
import it.mail.domain.core.quarkus.mailing.MailSenderImpl
import it.mail.domain.core.quarkus.mailing.PlainTextMailFactory
import it.mail.domain.core.quarkus.mailing.QuarkusMailSender
import it.mail.domain.model.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
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
    fun mailFactory(templateProcessor: TemplateProcessor) = MailFactoryManager(
        PlainTextMailFactory(),
        HtmlMailFactory(templateProcessor),
    )

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
