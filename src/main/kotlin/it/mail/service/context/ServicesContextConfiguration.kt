package it.mail.service.context

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.domain.MailMessageType
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.admin.HtmlMailMessageTypeFactory
import it.mail.service.admin.HtmlMailMessageTypeStateUpdater
import it.mail.service.admin.MailMessageTypeFactory
import it.mail.service.admin.MailMessageTypeFactoryManager
import it.mail.service.admin.MailMessageTypeService
import it.mail.service.admin.MailMessageTypeStateUpdater
import it.mail.service.admin.MailMessageTypeStateUpdaterManager
import it.mail.service.admin.PlainTextMailMessageTypeFactory
import it.mail.service.admin.PlainTextMailMessageTypeStateUpdater
import it.mail.service.external.ExternalMailMessageService
import it.mail.service.mailing.HungMailsResetManager
import it.mail.service.mailing.MailMessageService
import it.mail.service.mailing.MailSender
import it.mail.service.mailing.SendMailMessageService
import it.mail.service.mailing.UnsentMailProcessor
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
    fun mailSender(mailer: ReactiveMailer) = MailSender(mailer)

    @Singleton
    fun sendMailMessageService(
        mailSender: MailSender,
        mailMessageService: MailMessageService,
    ) = SendMailMessageService(mailSender, mailMessageService, CoroutineScope(Dispatchers.IO))

    fun stopSendMailMessageService(@Disposes mailMessageService: SendMailMessageService) {
        mailMessageService.stop()
    }

    @Singleton
    fun hungMailsResetManager(mailMessageService: MailMessageService) = HungMailsResetManager(mailMessageService)

    @Singleton
    fun unsentMailProcessor(
        mailMessageService: MailMessageService,
        sendService: SendMailMessageService,
    ) = UnsentMailProcessor(mailMessageService, sendService)
}
