package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.panache.common.Sort
import it.mail.domain.MailMessageType
import it.mail.service.model.Slice
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@Transactional
@ApplicationScoped
class MailMessageTypeRepository : PanacheRepository<MailMessageType> {

    fun findOneByName(name: String): MailMessageType? =
        find("name", name)
            .firstResult()

    fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val content = findAll(Sort.by("id"))
            .page(page, size)
            .list()

        return Slice(content, page, size)
    }

    fun existsOneWithName(name: String): Boolean =
        count("name", name) > 0
}
