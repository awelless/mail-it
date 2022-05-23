package it.mail.service.context

import io.quarkus.mailer.reactive.ReactiveMailer
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.admin.MailMessageTypeService
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
    fun mailMessageTypeService(mailMessageTypeRepository: MailMessageTypeRepository) = MailMessageTypeService(mailMessageTypeRepository)
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
