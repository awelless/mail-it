package it.mail.service.external

import it.mail.domain.MailMessageType
import it.mail.repository.MailMessageTypeRepository
import it.mail.service.NotFoundException
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ExternalMailMessageTypeService(
    private val mailMessageTypeRepository: MailMessageTypeRepository
) {

    fun getTypeByName(name: String): MailMessageType {
        return mailMessageTypeRepository.findOneByName(name)
            ?: throw NotFoundException("MailMessageType with name: $name is not found")
    }
}