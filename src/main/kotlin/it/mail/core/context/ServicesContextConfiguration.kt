package it.mail.core.context

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.core.admin.HtmlMailMessageTypeFactory
import it.mail.core.admin.HtmlMailMessageTypeStateUpdater
import it.mail.core.admin.MailMessageTypeFactory
import it.mail.core.admin.MailMessageTypeFactoryManager
import it.mail.core.admin.MailMessageTypeService
import it.mail.core.admin.MailMessageTypeStateUpdater
import it.mail.core.admin.MailMessageTypeStateUpdaterManager
import it.mail.core.admin.PlainTextMailMessageTypeFactory
import it.mail.core.admin.PlainTextMailMessageTypeStateUpdater
import it.mail.core.external.ExternalMailMessageService
import it.mail.core.mailing.HungMailsResetManager
import it.mail.core.mailing.MailMessageService
import it.mail.core.mailing.MailSender
import it.mail.core.mailing.SendMailMessageService
import it.mail.core.mailing.UnsentMailProcessor
import it.mail.core.model.MailMessageType
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
