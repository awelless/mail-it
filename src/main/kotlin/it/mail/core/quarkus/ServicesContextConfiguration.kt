package it.mail.core.quarkus

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.core.admin.mail.AdminMailMessageService
import it.mail.core.admin.type.HtmlMailMessageTypeFactory
import it.mail.core.admin.type.HtmlMailMessageTypeStateUpdater
import it.mail.core.admin.type.MailMessageTypeFactory
import it.mail.core.admin.type.MailMessageTypeFactoryManager
import it.mail.core.admin.type.MailMessageTypeService
import it.mail.core.admin.type.MailMessageTypeStateUpdater
import it.mail.core.admin.type.MailMessageTypeStateUpdaterManager
import it.mail.core.admin.type.PlainTextMailMessageTypeFactory
import it.mail.core.admin.type.PlainTextMailMessageTypeStateUpdater
import it.mail.core.external.ExternalMailMessageService
import it.mail.core.mailing.HungMailsResetManager
import it.mail.core.mailing.MailMessageService
import it.mail.core.mailing.MailSender
import it.mail.core.mailing.SendMailMessageService
import it.mail.core.mailing.UnsentMailProcessor
import it.mail.core.mailing.templates.TemplateProcessor
import it.mail.core.mailing.templates.TemplateProcessorManager
import it.mail.core.mailing.templates.freemarker.Configuration
import it.mail.core.mailing.templates.freemarker.FreemarkerTemplateProcessor
import it.mail.core.mailing.templates.freemarker.RepositoryTemplateLoader
import it.mail.core.mailing.templates.none.NoneTemplateProcessor
import it.mail.core.model.MailMessageType
import it.mail.core.quarkus.mailing.HtmlMailFactory
import it.mail.core.quarkus.mailing.MailFactory
import it.mail.core.quarkus.mailing.MailFactoryManager
import it.mail.core.quarkus.mailing.MailSenderImpl
import it.mail.core.quarkus.mailing.PlainTextMailFactory
import it.mail.core.quarkus.mailing.QuarkusMailSender
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.enterprise.inject.Disposes
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
    ) = MailMessageTypeService(mailMessageTypeRepository, mailMessageTypeFactory, mailMessageTypeStateUpdated)

    @Singleton
    fun adminMailMessageService(mailMessageRepository: MailMessageRepository) = AdminMailMessageService(mailMessageRepository)
}

class ExternalServicesContextConfiguration {

    @Singleton
    fun externalMailMessageService(
        mailMessageRepository: MailMessageRepository,
        mailMessageTypeRepository: MailMessageTypeRepository,
    ) = ExternalMailMessageService(mailMessageRepository, mailMessageTypeRepository)
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
        mailFactory: MailFactory,
        mailSender: MailSender,
        mailMessageService: MailMessageService,
    ) = SendMailMessageService(mailSender, mailMessageService, CoroutineScope(Dispatchers.IO))

    fun stopSendMailMessageService(@Disposes mailMessageService: SendMailMessageService) {
        mailMessageService.close()
    }

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
