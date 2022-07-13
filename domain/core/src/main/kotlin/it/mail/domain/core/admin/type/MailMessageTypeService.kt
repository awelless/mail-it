package it.mail.domain.core.admin.type

import it.mail.domain.core.NotFoundException
import it.mail.domain.core.ValidationException
import it.mail.domain.model.HtmlTemplateEngine
import it.mail.domain.model.MailMessageType
import it.mail.domain.model.MailMessageTypeState.DELETED
import it.mail.domain.model.MailMessageTypeState.FORCE_DELETED
import it.mail.domain.model.Slice
import it.mail.persistence.api.MailMessageTypeRepository
import mu.KLogging
import java.time.Instant

class MailMessageTypeService(
    private val mailMessageTypeRepository: MailMessageTypeRepository,
    private val mailMessageTypeFactory: MailMessageTypeFactory<MailMessageType>,
    private val mailMessageTypeStateUpdater: MailMessageTypeStateUpdater<MailMessageType>,
) {
    companion object : KLogging()

    suspend fun getById(id: Long): MailMessageType =
        mailMessageTypeRepository.findById(id)
            ?: throw it.mail.domain.core.NotFoundException("MailMessageType with id: $id is not found")

    suspend fun getAllSliced(page: Int, size: Int): Slice<MailMessageType> =
        mailMessageTypeRepository.findAllSliced(page, size)

    //    @Transactional // todo transaction doesn't work
    suspend fun createNewMailType(command: CreateMailMessageTypeCommand): MailMessageType {
        if (mailMessageTypeRepository.existsOneWithName(command.name)) {
            throw it.mail.domain.core.ValidationException("MailMessageType name: ${command.name} is not unique")
        }

        val mailType = mailMessageTypeFactory.create(command)

        mailMessageTypeRepository.create(mailType)

        logger.info { "Saved MailMessageType: ${mailType.id}" }

        return mailType
    }

    suspend fun updateMailType(command: UpdateMailMessageTypeCommand): MailMessageType {
        val mailType = getById(command.id)

        // updates mailType object data
        mailMessageTypeStateUpdater.update(mailType, command)

        // updates mailType in storage
        val updatedMailType = mailMessageTypeRepository.update(mailType)

        logger.info { "Updated MailMessageType: ${command.id}" }

        return updatedMailType
    }

    suspend fun deleteMailType(id: Long, force: Boolean) {
        val newState = if (force) {
            FORCE_DELETED
        } else {
            DELETED
        }

        if (mailMessageTypeRepository.updateState(id, newState, Instant.now()) == 0) {
            throw it.mail.domain.core.NotFoundException("MailMessageType with id: $id is not found")
        }

        if (force) {
            logger.info { "MailMessageType: $id is marked as force deleted" }
        } else {
            logger.info { "MailMessageType: $id is marked as deleted" }
        }
    }
}

class CreateMailMessageTypeCommand(
    val name: String,
    val description: String? = null,
    val maxRetriesCount: Int? = null,
    val contentType: MailMessageContentType,
    val templateEngine: HtmlTemplateEngine? = null,
    val template: String? = null,
)

class UpdateMailMessageTypeCommand(
    val id: Long,
    val description: String?,
    val maxRetriesCount: Int?,
    val templateEngine: HtmlTemplateEngine? = null,
    val template: String? = null,
)

enum class MailMessageContentType {

    PLAIN_TEXT,
    HTML,
}
