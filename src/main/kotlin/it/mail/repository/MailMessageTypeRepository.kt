package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.panache.common.Sort
import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState
import it.mail.service.model.Slice
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@Transactional
@ApplicationScoped
class MailMessageTypeRepository : PanacheRepository<MailMessageType> {

    fun findOneByName(name: String): MailMessageType? =
        find("name = ?1 AND state = ?2", name, MailMessageTypeState.ENABLED)
            .firstResult()

    fun findAllSliced(page: Int, size: Int): Slice<MailMessageType> {
        val content = find("state = ?1", Sort.by("id"), MailMessageTypeState.ENABLED)
            .page(page, size)
            .list()

        return Slice(content, page, size)
    }

    fun existsOneWithName(name: String): Boolean =
        count("name = ?1 AND state = ?2", name, MailMessageTypeState.ENABLED) > 0
}
