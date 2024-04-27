package io.mailit.worker.core.sending

import io.mailit.core.exception.NotFoundException
import io.mailit.value.MailId
import io.mailit.worker.api.SendMail
import io.mailit.worker.core.toDomainModel
import io.mailit.worker.core.Mail
import io.mailit.worker.core.toPersistenceModel
import io.mailit.worker.spi.mailing.MailSender
import io.mailit.worker.spi.persistence.MailRepository
import java.time.Clock
import mu.KLogging

internal class SendMailImpl(
    private val clock: Clock,
    private val mailRepository: MailRepository,
    private val mailSender: MailSender,
    private val sendingMailFactory: SendingMailFactory,
) : SendMail {

    override suspend fun invoke(id: MailId): Result<Unit> =
        mailRepository.findForSending(id)
            ?.let { sendMail(it.toDomainModel()) }
            ?: Result.failure(NotFoundException("Mail: ${id.value} not found"))

    private suspend fun sendMail(mail: Mail): Result<Unit> {
        logger.debug { "Sending mail: ${mail.id.value}" }

        if (mail.shouldBeCancelled()) {
            mail.onCancelledDelivery()
        } else {
            sendingMailFactory.create(mail)
                .map { mailSender.send(it) }
                .onSuccess { mail.onSuccessfulDelivery(clock.instant()) }
                .onFailure { mail.onFailedDelivery() }
        }

        return mailRepository.update(mail.toPersistenceModel())
    }

    companion object : KLogging()
}
