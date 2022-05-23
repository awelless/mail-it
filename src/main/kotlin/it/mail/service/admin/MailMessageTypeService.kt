package it.mail.service.admin

import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState.DELETED
import it.mail.domain.MailMessageTypeState.FORCE_DELETED
import it.mail.domain.Slice
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.NotFoundException
import it.mail.service.ValidationException
import mu.KLogging

class MailMessageTypeService(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    suspend fun getById(id: Long): MailMessageType =
        mailMessageTypeRepository.findById(id)
            ?: throw NotFoundException("MailMessageType with id: $id is not found")

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessageType> =
        mailMessageTypeRepository.findAllSliced(page, size)

//    @Transactional // todo transaction doesn't work
    suspend fun createNewMailType(name: String, description: String?, maxRetriesCount: Int?): MailMessageType {
        if (mailMessageTypeRepository.existsOneWithName(name)) {
            throw ValidationException("MailMessageType name: $name is not unique")
        }

        val mailType = MailMessageType(
            name = name,
            description = description,
            maxRetriesCount = maxRetriesCount,
        )

        mailMessageTypeRepository.create(mailType)

        logger.info { "Saved MailMessageType: ${mailType.id}" }

        return mailType
    }

    suspend fun updateMailType(id: Long, description: String?, maxRetriesCount: Int?): MailMessageType {
        val updatedRows = mailMessageTypeRepository.updateDescriptionAndMaxRetriesCount(
            id = id,
            description = description,
            maxRetriesCount = maxRetriesCount
        )

        if (updatedRows == 0) {
            throw NotFoundException("MailMessageType with id: $id is not found")
        }

        logger.info { "Updated MailMessageType: $id" }

        return getById(id)
    }

    suspend fun deleteMailType(id: Long, force: Boolean) {
        val newState = if (force) { FORCE_DELETED } else { DELETED }

        if (mailMessageTypeRepository.updateState(id, newState) == 0) {
            throw NotFoundException("MailMessageType with id: $id is not found")
        }

        if (force) {
            logger.info { "MailMessageType: $id is marked as force deleted" }
        } else {
            logger.info { "MailMessageType: $id is marked as deleted" }
        }
    }
}
