package io.mailit.core.service.mail.type

import io.mailit.core.api.admin.type.CreateMailMessageTypeCommand
import io.mailit.core.api.admin.type.MailMessageTypeService
import io.mailit.core.api.admin.type.UpdateMailMessageTypeCommand
import io.mailit.core.model.MailMessageType
import io.mailit.core.model.MailMessageTypeState.DELETED
import io.mailit.core.model.MailMessageTypeState.FORCE_DELETED
import io.mailit.core.spi.MailMessageTypeRepository
import io.mailit.value.MailTypeId
import io.mailit.value.Slice
import io.mailit.value.exception.DuplicateUniqueKeyException
import io.mailit.value.exception.NotFoundException
import io.mailit.value.exception.ValidationException
import java.time.Instant
import mu.KLogging

class MailMessageTypeServiceImpl(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
    private val mailMessageTypeFactory: MailMessageTypeFactory<MailMessageType>,
    private val mailMessageTypeStateUpdater: MailMessageTypeStateUpdater<MailMessageType>,
) : MailMessageTypeService {
    companion object : KLogging()

    override suspend fun getById(id: MailTypeId): MailMessageType =
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

    override suspend fun deleteMailType(id: MailTypeId, force: Boolean) {
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
