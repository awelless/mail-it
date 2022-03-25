package it.mail.service.admin

import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState.DELETED
import it.mail.domain.MailMessageTypeState.FORCE_DELETED
import it.mail.repository.MailMessageTypeRepository
import it.mail.service.BadRequestException
import it.mail.service.NotFoundException
import it.mail.service.model.Slice
import mu.KLogging
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class MailMessageTypeService(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
) {
    companion object : KLogging()

    fun getById(id: Long): MailMessageType =
        mailMessageTypeRepository.findById(id)
            ?: throw NotFoundException("MailMessageType with id: $id is not found")

    fun getAllSliced(page: Int, size: Int): Slice<MailMessageType> =
        mailMessageTypeRepository.findAllSliced(page, size)

    @Transactional
    fun createNewMailType(name: String, description: String?, maxRetriesCount: Int?): MailMessageType {
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

    @Transactional
    fun updateMailType(id: Long, description: String?, maxRetriesCount: Int?): MailMessageType {
        val mailType = getById(id)

        mailType.description = description
        mailType.maxRetriesCount = maxRetriesCount

        mailMessageTypeRepository.persist(mailType)

        logger.info { "Updated MailMessageType: ${mailType.id}" }

        return mailType
    }

    @Transactional
    fun deleteMailType(id: Long, force: Boolean) {
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
