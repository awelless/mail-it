package it.mail.service.admin

import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState.DELETED
import it.mail.domain.MailMessageTypeState.FORCE_DELETED
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.service.BadRequestException
import it.mail.service.NotFoundException
import it.mail.service.model.Slice
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
            throw BadRequestException("MailMessageType name: $name is not unique")
        }

        val mailType = MailMessageType(
            name = name,
            description = description,
            maxRetriesCount = maxRetriesCount,
        )

        mailMessageTypeRepository.persist(mailType)

        logger.info { "Saved MailMessageType: ${mailType.id}" }

        return mailType
    }

//    @Transactional // todo transaction doesn't work
    suspend fun updateMailType(id: Long, description: String?, maxRetriesCount: Int?): MailMessageType {
        val mailType = getById(id)

        mailType.description = description
        mailType.maxRetriesCount = maxRetriesCount

        mailMessageTypeRepository.persist(mailType)

        logger.info { "Updated MailMessageType: ${mailType.id}" }

        return mailType
    }

    // @Transactional
    suspend fun deleteMailType(id: Long, force: Boolean) {
        val mailType = getById(id)

        mailType.state = if (force) { FORCE_DELETED } else { DELETED }

        mailMessageTypeRepository.persist(mailType)

        if (force) {
            logger.info { "MailMessageType: ${mailType.id} is marked as force deleted" }
        } else {
            logger.info { "MailMessageType: ${mailType.id} is marked as deleted" }
        }
    }
}
