package it.mail.domain.core.admin.type

import it.mail.domain.admin.api.type.CreateMailMessageTypeCommand
import it.mail.domain.admin.api.type.MailMessageTypeService
import it.mail.domain.admin.api.type.UpdateMailMessageTypeCommand
import it.mail.domain.model.MailMessageType
import it.mail.domain.model.MailMessageTypeState.DELETED
import it.mail.domain.model.MailMessageTypeState.FORCE_DELETED
import it.mail.domain.model.Slice
import it.mail.exception.NotFoundException
import it.mail.exception.ValidationException
import it.mail.persistence.api.MailMessageTypeRepository
import mu.KLogging
import java.time.Instant

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

    //    @Transactional // todo transaction doesn't work
    override suspend fun createNewMailType(command: CreateMailMessageTypeCommand): MailMessageType {
        if (mailMessageTypeRepository.existsOneWithName(command.name)) {
            throw ValidationException("MailMessageType name: ${command.name} is not unique")
        }

        val mailType = mailMessageTypeFactory.create(command)

        mailMessageTypeRepository.create(mailType)

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
