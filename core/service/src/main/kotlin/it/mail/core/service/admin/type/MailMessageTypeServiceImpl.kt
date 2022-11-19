package it.mail.core.service.admin.type

import it.mail.core.admin.api.type.CreateMailMessageTypeCommand
import it.mail.core.admin.api.type.MailMessageTypeService
import it.mail.core.admin.api.type.UpdateMailMessageTypeCommand
import it.mail.core.exception.NotFoundException
import it.mail.core.exception.ValidationException
import it.mail.core.model.MailMessageType
import it.mail.core.model.MailMessageTypeState.DELETED
import it.mail.core.model.MailMessageTypeState.FORCE_DELETED
import it.mail.core.model.Slice
import it.mail.core.persistence.api.DuplicateUniqueKeyException
import it.mail.core.persistence.api.MailMessageTypeRepository
import java.time.Instant
import mu.KLogging

class MailMessageTypeServiceImpl(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
    private val mailMessageTypeFactory: MailMessageTypeFactory<MailMessageType>,
    private val mailMessageTypeStateUpdater: MailMessageTypeStateUpdater<MailMessageType>,
) : MailMessageTypeService {
    companion object : KLogging()

    override suspend fun getById(id: Long): MailMessageType =
        mailMessageTypeRepository.findById(id)
            ?: throw NotFoundException("MailMessageType with id: $id is not found")

    override suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessageType> =
        mailMessageTypeRepository.findAllSliced(page, size)

    override suspend fun createNewMailType(command: CreateMailMessageTypeCommand): MailMessageType {
        val mailType = mailMessageTypeFactory.create(command)

        try {
            mailMessageTypeRepository.create(mailType)
        } catch (e: DuplicateUniqueKeyException) {
            throw ValidationException("MailMessageType name: ${command.name} is not unique", e)
        }

        logger.info { "Saved MailMessageType: ${mailType.id}" }

        return mailType
    }

    override suspend fun updateMailType(command: UpdateMailMessageTypeCommand): MailMessageType {
        val mailType = getById(command.id)

        // updates mailType object data
        mailMessageTypeStateUpdater.update(mailType, command)

        // updates mailType in storage
        val updatedMailType = mailMessageTypeRepository.update(mailType)

        logger.info { "Updated MailMessageType: ${command.id}" }

        return updatedMailType
    }

    override suspend fun deleteMailType(id: Long, force: Boolean) {
        val newState = if (force) {
            FORCE_DELETED
        } else {
            DELETED
        }

        if (mailMessageTypeRepository.updateState(id, newState, Instant.now()) == 0) {
            throw NotFoundException("MailMessageType with id: $id is not found")
        }

        if (force) {
            logger.info { "MailMessageType: $id is marked as force deleted" }
        } else {
            logger.info { "MailMessageType: $id is marked as deleted" }
        }
    }
}
