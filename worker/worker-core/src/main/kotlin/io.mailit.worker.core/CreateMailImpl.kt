package io.mailit.worker.core

import io.mailit.core.exception.DuplicateUniqueKeyException
import io.mailit.core.exception.ValidationException
import io.mailit.idgenerator.api.IdGenerator
import io.mailit.lang.recoverIf
import io.mailit.value.MailId
import io.mailit.value.MailState
import io.mailit.worker.api.CreateMail
import io.mailit.worker.api.CreateMailRequest
import io.mailit.worker.spi.persistence.MailRepository
import io.mailit.worker.spi.persistence.MailTypeRepository
import io.mailit.worker.spi.persistence.PersistenceMail
import java.time.Clock
import mu.KLogging

internal class CreateMailImpl(
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val mailRepository: MailRepository,
    private val mailTypeRepository: MailTypeRepository,
) : CreateMail {

    override suspend fun invoke(request: CreateMailRequest): Result<Unit> {
        val mailTypeId = mailTypeRepository.findIdByName(request.mailTypeName)
            ?: return Result.failure(ValidationException("Invalid type: ${request.mailTypeName} is passed"))

        val mail = PersistenceMail(
            id = MailId(idGenerator.generateId()),
            mailTypeId = mailTypeId,
            text = request.text,
            data = request.data,
            subject = request.subject,
            emailFrom = request.emailFrom,
            emailTo = request.emailTo,
            createdAt = clock.instant(),
            sendingStartedAt = null,
            sentAt = null,
            state = MailState.PENDING,
            failedCount = 0,
            deduplicationId = request.deduplicationId,
        )

        return mailRepository.create(mail)
            .onSuccess { logger.debug { "Persisted message with id: ${mail.id}" } }
            .recoverIf { _: DuplicateUniqueKeyException -> logger.debug { "Mail with deduplication id: ${request.deduplicationId} has already been created" } }
    }

    companion object : KLogging()
}
